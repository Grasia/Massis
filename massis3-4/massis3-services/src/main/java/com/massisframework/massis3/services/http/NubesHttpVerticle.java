package com.massisframework.massis3.services.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.aesteve.vertx.nubes.VertxNubes;
import com.github.jknack.handlebars.Handlebars;
import com.massisframework.massis3.services.term.MassisShellServiceImpl;
import com.massisframework.massis3.services.term.VertxCommandHelper;
import com.massisframework.massis3.services.term.commands.MassisCommandResolver;
import com.massisframework.massis3.web.injectors.EventBusServiceInjector;
import com.massisframework.massis3.web.injectors.EventBusSimulationService;
import com.massisframework.massis3.web.injectors.MassisAPIResponseInjector;
import com.massisframework.massis3.web.injectors.ServiceDiscoveryInjector;
import com.massisframework.massis3.web.response.MassisAPIResponse;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.shiro.PropertiesProviderConstants;
import io.vertx.ext.auth.shiro.ShiroAuth;
import io.vertx.ext.auth.shiro.ShiroAuthOptions;
import io.vertx.ext.auth.shiro.ShiroAuthRealmType;
import io.vertx.ext.shell.ShellService;
import io.vertx.ext.shell.ShellServiceOptions;
import io.vertx.ext.shell.term.HttpTermOptions;
import io.vertx.ext.shell.term.impl.HttpTermServer;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.sockjs.BridgeEventType;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.servicediscovery.ServiceDiscovery;

public class NubesHttpVerticle extends AbstractVerticle {

	private static final Logger log = LoggerFactory.getLogger(NubesHttpVerticle.class);
	private Router mainRouter;
	private VertxNubes nubes;
	private HttpServer server;
	private Router apiRouter;
	private ServiceDiscovery discovery;
	private int port;
	private String authPropertiesFile;
	private String host;

	@Override
	public void start(Future<Void> startFuture) throws Exception
	{
		try
		{
			this.host = config().getString("host");
			this.port = config().getInteger("port");
			this.authPropertiesFile = config().getString("authPropertiesFile");

			this.discovery = ServiceDiscovery.create(vertx);
			this.mainRouter = Router.router(vertx);
			this.apiRouter = Router.router(vertx);
			this.mainRouter.mountSubRouter("/api", apiRouter);
			bindNubesRouter();
			bindTermRoutes();
			List<String> routes = new ArrayList<>();
			this.apiRouter.getRoutes().stream().map(Route::getPath).forEach(routes::add);
			log.info("Routers binded: " + routes);
			configureEB1();
			Future<Void> fServer = Future.future();
			startServer(fServer.completer());
			fServer.map((Void) null).setHandler(startFuture);
		} catch (Exception e)
		{
			log.error("Exception when starting nubes verticle.", e);
			e.printStackTrace();
			startFuture.fail(e);
			return;
		}
	}

	private void bindTermRoutes()
	{
		Router termRouter = Router.router(vertx);
		this.apiRouter.mountSubRouter("/term", termRouter);
		termRouter.get("/").handler(ctx -> {
			ctx.reroute("/api/term/shell.html");
		});
		termRouter.route().handler(BodyHandler.create());

		Handlebars handlebars = new Handlebars();
		String shellHtmlRes = "Error when retrieving shell interface";
		try
		{
			HashMap<Object, Object> shellConfigMap = new HashMap<>();
			// TODO hardcoded
			shellConfigMap.put("termMountPoint", "/api/term");
			shellHtmlRes = handlebars.compile("/web/shell").apply(shellConfigMap);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		HttpTermOptions httpOptions = new HttpTermOptions()
				.setShellHtmlResource(Buffer.buffer(shellHtmlRes));

		ShellServiceOptions sso = new ShellServiceOptions()
				.setWelcomeMessage(VertxCommandHelper.welcomeText());

		ShellService ss = new MassisShellServiceImpl(vertx, sso);
		HttpTermServer termServer = new HttpTermServer(vertx, termRouter, httpOptions);
		ss.server().registerTermServer(termServer);
		ss.start(r -> {
			if (r.failed())
			{
				log.error("Starting shell server failed", r.cause());
			} else
			{

				if (log.isInfoEnabled())
				{
					log.info("Http term server started");
				}

				ss.server().registerCommandResolver(new MassisCommandResolver(vertx));
			}
		});
	}

	private void startServer(Handler<AsyncResult<Void>> handler)
	{
		HttpServerOptions options = new HttpServerOptions();
		options.setPort(this.port);
		options.setCompressionSupported(true);
		options.setHost(this.host);
		this.server = vertx.createHttpServer(options)
				// .websocketHandler(this::websocketHandler)
				.requestHandler(this.mainRouter::accept)
				.listen(r -> {
					if (r.failed())
					{
						log.error("HTTP Server deployment failed ", r.cause());
						handler.handle(Future.failedFuture(r.cause()));
						return;
					} else
					{
						if (log.isInfoEnabled())
						{
							log.info("HTTP Service verticle started. Listening on port {}",
									r.result().actualPort());
						}
						handler.handle(Future.succeededFuture());
					}
				});
	}

	private void bindNubesRouter()
	{
		JsonObject cfg = new JsonObject().put("src-package", getClass().getPackage().getName())
				.put("domain-package", "com.massisframework.massis3.services.dataobjects")
				.put("templates", new JsonArray().add("hbs"));

		this.nubes = new VertxNubes(vertx, cfg);
		registerInjectors(nubes);
		configureAuth(nubes);
		this.nubes.bootstrap(r -> {
			if (r.failed())
			{
				log.error("Nubes bootstrap failed", r.cause());
			} else
			{

				if (log.isInfoEnabled())
				{
					log.info("Nubes bootstrapped successfully");
				}

			}
		}, apiRouter);
	}

	private void configureEB1()
	{
		BridgeOptions options = new BridgeOptions()
				.addOutboundPermitted(new PermittedOptions().setAddress("zz"));

		this.mainRouter.route("/eventbus/*")
				.handler(SockJSHandler.create(vertx).bridge(options, event -> {

					if (event.type() == BridgeEventType.SOCKET_CREATED)
					{
						System.out.println("A socket was created");
					}
					// This signals that it's ok to process the event
					event.complete(true);
				}));
	}

	private void configureAuth(VertxNubes nubes)
	{

		ShiroAuthOptions shiroOptions = new ShiroAuthOptions();
		shiroOptions.setType(ShiroAuthRealmType.PROPERTIES);
		shiroOptions.setConfig(new JsonObject().put(
				PropertiesProviderConstants.PROPERTIES_PROPS_PATH_FIELD, authPropertiesFile));
		AuthProvider authProvider = ShiroAuth.create(vertx, shiroOptions);
		nubes.setAuthProvider(authProvider);

	}

	private void registerInjectors(VertxNubes nubes)
	{
		nubes.registerAnnotatedParamInjector(
				EventBusSimulationService.class,
				new EventBusServiceInjector());
		nubes.registerTypeParamInjector(MassisAPIResponse.class, new MassisAPIResponseInjector());
		nubes.registerTypeParamInjector(ServiceDiscovery.class,
				new ServiceDiscoveryInjector(discovery));
	}

	@Override
	public void stop(Future<Void> stopFuture) throws Exception
	{
		this.unbindNubes(r -> {
			// ..
			if (r.failed())
			{
				log.error("Stopping nubes failed");
			}

		});
		this.stopServer(r -> {
			if (r.failed())
			{
				log.error("Stopping server failed");
			}
		});

		// TODO
		stopFuture.complete();
	}

	private void stopServer(Handler<AsyncResult<Void>> handler)
	{
		this.server.close(handler);

	}

	private void unbindNubes(Handler<AsyncResult<Void>> handler)
	{
		this.nubes.stop(handler);
		this.discovery.close();
	}

	public static void main(String[] args)
	{
		Vertx.vertx().deployVerticle(NubesHttpVerticle.class.getName(),
				new DeploymentOptions()
						.setConfig(new JsonObject().put("host", "localhost").put("port", 8080)));
	}
}
