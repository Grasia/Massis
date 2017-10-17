package com.massisframework.massis3.simulation.server;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.massisframework.massis3.commons.app.server.AppSystemManager;
import com.massisframework.massis3.commons.app.server.MassisSystem;
import com.massisframework.massis3.commons.app.server.impl.VertxAppSystemManagerImpl;
import com.massisframework.massis3.core.config.SimulationExecutionConfig;
import com.massisframework.massis3.core.systems.debug.DebugSystem;
import com.massisframework.massis3.core.systems.required.EntityDataSystem;
import com.massisframework.massis3.core.systems.required.SceneLoaderSystem;
import com.massisframework.massis3.services.dataobjects.JsonPoint;
import com.massisframework.massis3.services.eventbus.Massis3ServiceUtils;
import com.massisframework.massis3.services.eventbus.sim.EnvironmentService;
import com.massisframework.massis3.services.eventbus.sim.HumanAgentService;
import com.massisframework.massis3.services.eventbus.sim.SystemsService;
import com.massisframework.massis3.simulation.SimulationSystems;
import com.massisframework.massis3.simulation.server.eventbus.services.EnvironmentServiceImpl;
import com.massisframework.massis3.simulation.server.eventbus.services.HumanAgentServiceImpl;
import com.massisframework.massis3.simulation.server.eventbus.services.SystemsServiceImpl;
import com.simsilica.es.base.DefaultEntityData;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Verticle;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.serviceproxy.ProxyHelper;

public class SimulationExecutionVerticle extends AbstractVerticle {

	private static final Logger log = LoggerFactory.getLogger(SimulationExecutionVerticle.class);

	private List<String> assetFolders;
	private String sceneFile;
	private boolean debugEnabled;
	private long simulationId;
	private AppSystemManager sManager;

	// TODO generalize or make an api
	private ConcurrentLinkedDeque<Runnable> closeTasks = new ConcurrentLinkedDeque<>();

	@Override
	public void start(Future<Void> startFuture) throws Exception
	{

		if (log.isInfoEnabled())
		{
			log.info("Starting Simulation execution verticle");
		}

		SimulationExecutionConfig simConfig = new SimulationExecutionConfig(this.config());
		this.assetFolders = simConfig.getAssetFolders();
		this.sceneFile = simConfig.getSceneFile();
		this.debugEnabled = simConfig.isDebugEnabled();
		this.simulationId = simConfig.getSimulationId();
		/*
		 * Launch simulation
		 */
		List<MassisSystem> massisStates = createMassisSystems();

		this.sManager = new VertxAppSystemManagerImpl(vertx, false, massisStates);

		if (log.isInfoEnabled())
		{
			log.info("Initializing SystemManager");
		}

		Future.succeededFuture()
				.compose(_void -> initializeSystemsManager())
				.compose(_void -> initServices())
				.compose(_void -> startSimulationLoop())
				.compose(_void -> createMainCamera())
				//
				.setHandler(startFuture.completer());

	}

	private Future<Void> startSimulationLoop()
	{
		Future<Void> f = Future.future();
		this.sManager.start(f.completer());
		this.addCloseTask(() -> this.sManager.shutdown());
		return f;
	}

	private Future<Void> initializeSystemsManager()
	{
		Future<Void> initFuture = Future.future();
		this.sManager.initialize(initFuture.completer());
		return initFuture;
	}

	private Future<Void> createMainCamera()
	{
		Future<String> addCamF = Future.future();
		EnvironmentService envS = Massis3ServiceUtils
				.createProxy(vertx, EnvironmentService.class, this.simulationId);
		envS.addCamera(addCamF.completer());

		return addCamF.compose(camId -> {
			Future<Void> f = Future.future();
			envS.setCameraLocation(camId,
					new JsonPoint(12.885475f, 1.1523216f, 10.428574f),
					f.completer());
			return f;
		});

	}

	private List<MassisSystem> createMassisSystems()
	{
		SceneLoaderSystem sceneLoaderState = new SceneLoaderSystem(assetFolders, sceneFile);
		EntityDataSystem entityDataState = new EntityDataSystem(new DefaultEntityData());
		List<MassisSystem> massisStates = SimulationSystems
				.instantiate(SimulationSystems.defaultSystems());

		massisStates.add(sceneLoaderState);
		massisStates.add(entityDataState);

		if (!this.debugEnabled)
		{
			massisStates.stream()
					.filter(DebugSystem.class::isInstance)
					.forEach(s -> s.setEnabled(false));
		}
		return massisStates;
	}

	private Future<Void> initServices()
	{
		if (log.isInfoEnabled())
		{
			log.info("Deploying services");
		}
		return CompositeFuture.all(
				deploy(HumanAgentService.class, new HumanAgentServiceImpl(this.sManager)),
				deploy(EnvironmentService.class, new EnvironmentServiceImpl(this.sManager)),
				deploy(SystemsService.class, new SystemsServiceImpl(sManager)))
				//
				.map((Void) null);

	}

	private <T> Future<Void> deploy(Class<T> clazz, T service)
	{
		Future<Void> f = Future.future();
		deploy(clazz, service, f.completer());
		return f;
	}

	private <T> void deploy(Class<T> clazz, T service, Handler<AsyncResult<Void>> handler)
	{
		if (service instanceof Verticle)
		{
			DeploymentOptions options = new DeploymentOptions();
			options.setConfig(this.config().copy());
			vertx.deployVerticle((Verticle) service, options, r -> {
				if (r.failed())
				{
					log.error("Deploying service failed", r.cause());
					handler.handle(Future.failedFuture(r.cause()));
				} else
				{
					registerService(clazz, service, handler);
					addCloseTask(() -> vertx.undeploy(r.result()));
				}
			});
		} else
		{
			registerService(clazz, service, handler);
		}

	}

	private <T> void registerService(Class<T> clazz, T service, Handler<AsyncResult<Void>> handler)
	{
		String serviceGroup = String.valueOf(this.simulationId);
		String endpoint = Massis3ServiceUtils.defaultSimulationServiceAddress(clazz, serviceGroup);
		MessageConsumer<JsonObject> serviceConsumer = ProxyHelper.registerService(clazz, vertx,
				service, endpoint);
		Massis3ServiceUtils.publishService(vertx, clazz, serviceGroup, ar -> {
			if (ar.succeeded())
			{
				if (log.isInfoEnabled())
				{
					log.info("Service publication OK. Published record: {}",
							ar.result().toJson().encode());

					final String registration = ar.result().getRegistration();
					addCloseTask(() -> unregisterService(registration));
					handler.handle(Future.succeededFuture());

				}
			} else
			{
				handler.handle(Future.failedFuture(ar.cause()));
			}
		});
		addCloseTask(() -> {
			serviceConsumer.unregister();
		});

	}

	private void unregisterService(String registration)
	{
		ServiceDiscovery discovery = ServiceDiscovery.create(vertx);
		discovery.unpublish(registration, ar -> {
			if (ar.failed())
			{
				log.error("Service unpublishing failed", ar.cause());
			} else
			{
				if (log.isInfoEnabled())
				{
					log.info("Service unpublished: {}", registration);
				}
			}
			discovery.close();
		});

	}

	@Override
	public void stop() throws Exception
	{
		while (!this.closeTasks.isEmpty())
		{
			try
			{
				closeTasks.poll().run();
			} catch (Exception e)
			{
				log.error("Close task failed", e);
			}
		}
	}

	private void addCloseTask(Runnable task)
	{
		this.closeTasks.add(task);
	}
}
