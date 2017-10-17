package com.massisframework.massis3.simulation.server;

import static com.massisframework.massis3.services.eventbus.Massis3ServiceUtils.registerDefaultAndPublishRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.massisframework.massis3.core.config.SimulationExecutionConfig;
import com.massisframework.massis3.core.config.SimulationServerConfig;
import com.massisframework.massis3.services.eventbus.EchoService;
import com.massisframework.massis3.services.eventbus.Massis3ServiceUtils;
import com.massisframework.massis3.services.eventbus.SimulationServerService;
import com.massisframework.massis3.services.eventbus.impl.EchoServiceImpl;
import com.massisframework.massis3.services.http.NubesHttpVerticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;

public class SimulationServerVerticle extends AbstractVerticle
		implements SimulationServerService {

	private static final Logger log = LoggerFactory.getLogger(SimulationServerVerticle.class);
	private Map<Long, String> simulations;
	private SimulationServerConfig serverConfig;
	private Map<String, String> availableScenes;
	private List<String> assetFolders;
	private static AtomicLong friendlyCounter = new AtomicLong();

	@Override
	public void start(Future<Void> startFuture) throws Exception
	{
		this.serverConfig = new SimulationServerConfig(config());
		this.simulations = new ConcurrentHashMap<>();
		this.assetFolders = this.serverConfig.getAssetFolders();
		this.availableScenes = retrieveAvailableScenes();

		Future.succeededFuture()
				.compose(_void -> {
					Future<Void> f = Future.future();
					deploySimulationServerService(f.completer());
					return f;
				})
				.compose(_void -> {
					Future<Void> f = Future.future();
					deployEchoService(f.completer());
					return f;
				}).compose(_void -> {
					Future<Void> f = Future.future();
					if (log.isInfoEnabled())
					{
						log.info("Deploying services verticle");
					}
					deployServicesVerticle(f.completer());
					return f;
				}).setHandler(startFuture.completer());

	}

	private void deploySimulationServerService(Handler<AsyncResult<Void>> handler)
	{
		Massis3ServiceUtils.registerDefaultAndPublishRecord(vertx,
				SimulationServerService.class, Massis3ServiceUtils.GLOBAL_SERVICE_GROUP, this,
				ar -> {
					if (ar.failed())
					{
						handler.handle(Future.failedFuture(ar.cause()));
					} else
					{

						if (log.isInfoEnabled())
						{
							log.info("Deployed simulation server service. Record: {}", ar.result());
						}

						handler.handle(Future.succeededFuture());
					}
				});

	}

	private void deployServicesVerticle(Handler<AsyncResult<Void>> handler)
	{

		JsonObject cfg = new JsonObject();
		cfg.put("port", this.serverConfig.getHttpServerConfig().getPort());
		cfg.put("host", this.serverConfig.getHttpServerConfig().getHost());
		// cfg.put("streamsMountPoint", "/streams");
		// cfg.put("termMountPoint", "/term");
		cfg.put("authPropertiesFile", this.serverConfig.getAuthPropertiesFile());
		DeploymentOptions options = new DeploymentOptions().setConfig(cfg);
		Future<String> deployF = Future.future();
		 vertx.deployVerticle(NubesHttpVerticle.class.getName(), options,
		 deployF.completer());
		deployF.map((Void) null).setHandler(handler);

	}

	private void deployEchoService(Handler<AsyncResult<Void>> handler)
	{
		EchoServiceImpl service = new EchoServiceImpl(vertx);
		String group = Massis3ServiceUtils.GLOBAL_SERVICE_GROUP;
		registerDefaultAndPublishRecord(vertx, EchoService.class, group, service, ar -> {
			if (ar.succeeded())
			{
				Record publishedRecord = ar.result();

				if (log.isInfoEnabled())
				{
					log.info("Service publication OK. Published record: {}",
							publishedRecord.toJson().encode());
				}
				handler.handle(Future.succeededFuture());
			} else
			{
				log.error("Service publication failed. ", ar.cause());
				handler.handle(Future.failedFuture(ar.cause()));
			}
		});
	}

	private Map<String, String> retrieveAvailableScenes() throws IOException
	{
		Map<String, String> sceneAliases = new HashMap<>();
		for (String af : this.assetFolders)
		{
			Path root = Paths.get(af);
			Files.walk(root)
					.filter(Files::isRegularFile)
					.filter(p -> p.toString().endsWith(".sh3d"))
					.forEach(path -> {
						String absPath = root.relativize(path).toString();
						String name = FilenameUtils.getBaseName(absPath);
						sceneAliases.put(name, absPath);
					});
		}
		return sceneAliases;

	}

	@Override
	public void stop() throws Exception
	{
	}

	@Override
	public void activeSimulations(Handler<AsyncResult<JsonArray>> resultHandler)
	{
		JsonArray res = new JsonArray();
		this.simulations.forEach((simId, verticleId) -> {
			res.add(simId);
		});
		resultHandler.handle(Future.succeededFuture(res));
	}

	@Override
	public void create(String sceneFile, Handler<AsyncResult<Long>> resultHandler)
	{
		// check scene file. What about config???
		if (!this.availableScenes.containsKey(sceneFile))
		{
			resultHandler.handle(Future.failedFuture("Scene provided (\"" + sceneFile
					+ "\") not found. Available scenes: " + this.availableScenes.keySet()));
			return;
		}
		sceneFile = this.availableScenes.get(sceneFile);

		DeploymentOptions options = new DeploymentOptions();
		long simulationId = friendlyCounter.getAndIncrement();
		SimulationExecutionConfig cfg = new SimulationExecutionConfig()
				.withAssetFolders(this.assetFolders)
				.withDebugEnabled(false)
				.withSceneFile(sceneFile)
				.withSimulationId(simulationId);
		options.setConfig(cfg.toJson());
		// options.setWorker(true);
		this.vertx.deployVerticle(SimulationExecutionVerticle.class.getName(), options, r -> {
			if (r.failed())
			{
				log.error("Error when deploying verticle", r.cause());
				resultHandler.handle(Future.failedFuture(r.cause()));
			} else
			{
				if (log.isInfoEnabled())
				{
					log.info("Simulation verticle deployed");
				}
				this.simulations.put(simulationId, r.result());
				resultHandler.handle(Future.succeededFuture(simulationId));
			}
		});
	}

	@Override
	public void destroy(long simId, Handler<AsyncResult<Void>> resultHandler)
	{
		String verticleId = this.simulations.remove(simId);
		if (verticleId == null)
		{
			resultHandler.handle(Future.failedFuture(
					"Simulation id provided (" + simId + ") was not registered in the server"));
			return;
		}
		this.vertx.undeploy(verticleId, r -> {
			if (r.failed())
			{
				resultHandler.handle(Future.failedFuture(r.cause()));
			} else
			{
				resultHandler.handle(Future.succeededFuture());
			}
		});
	}

	@Override
	public void availableScenes(Handler<AsyncResult<JsonArray>> resultHandler)
	{
		JsonArray resp = new JsonArray();
		this.availableScenes.keySet().forEach(resp::add);
		resultHandler.handle(Future.succeededFuture(resp));
	}

}
