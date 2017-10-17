package com.massisframework.massis3.web;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.aesteve.vertx.nubes.VertxNubes;
import com.massisframework.massis3.web.injectors.EventBusServiceInjector;
import com.massisframework.massis3.web.injectors.EventBusSimulationService;
import com.massisframework.massis3.web.injectors.MassisAPIResponseInjector;
import com.massisframework.massis3.web.injectors.ServiceDiscoveryInjector;
import com.massisframework.massis3.web.response.MassisAPIResponse;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.shiro.PropertiesProviderConstants;
import io.vertx.ext.auth.shiro.ShiroAuth;
import io.vertx.ext.auth.shiro.ShiroAuthOptions;
import io.vertx.ext.auth.shiro.ShiroAuthRealmType;
import io.vertx.ext.web.Router;
import io.vertx.servicediscovery.ServiceDiscovery;

public class Massis3WebVerticle extends AbstractVerticle {

	private static final Logger log = LoggerFactory.getLogger(Massis3WebVerticle.class);

	private VertxNubes nubes;
	private int port;
	private String host;
	private JsonObject webappCfg;
	private HttpServerOptions httpOptions;
	private ServiceDiscovery discovery;
	private String authFile;

	private Router mainRouter;

	@Override
	public void start(Future<Void> startFuture) throws Exception
	{

		this.port = config().getInteger("port");
		this.host = config().getString("host");
		this.authFile = config().getString("authFile");
		this.webappCfg = config().getJsonObject("nubes");
		this.httpOptions = new HttpServerOptions()
				.setPort(this.port)
				.setHost(host);

		this.nubes = new VertxNubes(vertx, this.webappCfg);
		this.mainRouter = Router.router(vertx);

		vertx.createHttpServer(httpOptions).requestHandler(this.mainRouter::accept).listen(r -> {
			bindNubes();
		});

		super.start();
	}

	@Override
	public void stop() throws Exception
	{
		this.discovery.close();
	}

	private void bindNubes()
	{
		this.mainRouter.route("/").handler(ctx -> ctx.reroute("/dashboard"));
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
		}, mainRouter);
	}

	private void configureAuth(VertxNubes nubes)
	{

		ShiroAuthOptions shiroOptions = new ShiroAuthOptions();
		shiroOptions.setType(ShiroAuthRealmType.PROPERTIES);
		shiroOptions.setConfig(new JsonObject().put(
				PropertiesProviderConstants.PROPERTIES_PROPS_PATH_FIELD, authFile));
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

	public static void main(String[] args) throws Exception
	{
		JsonObject cfg = new JsonObject(Buffer.buffer(Files
				.readAllBytes(Paths
						.get(Massis3WebVerticle.class.getResource("/webapp/config.json")
								.toURI()))));
		Vertx vertx = Vertx.vertx();

		vertx.deployVerticle(Massis3WebVerticle.class.getName(),
				new DeploymentOptions().setConfig(cfg));
	}
}
