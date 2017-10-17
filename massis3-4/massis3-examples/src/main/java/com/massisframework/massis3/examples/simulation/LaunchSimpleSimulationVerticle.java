package com.massisframework.massis3.examples.simulation;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.math.Vector3f;
import com.massisframework.massis3.core.config.HttpServerConfig;
import com.massisframework.massis3.core.config.SimulationServerConfig;
import com.massisframework.massis3.core.config.SimulationServerConfig.RenderMode;
import com.massisframework.massis3.core.config.SimulationServerConfig.RendererType;
import com.massisframework.massis3.services.dataobjects.JsonPoint;
import com.massisframework.massis3.services.eventbus.Massis3ServiceUtils;
import com.massisframework.massis3.services.eventbus.SimulationServerService;
import com.massisframework.massis3.services.eventbus.sim.EnvironmentService;
import com.massisframework.massis3.services.eventbus.sim.HumanAgentService;
import com.massisframework.massis3.simulation.server.SimulationServerLauncher;
import com.massisframework.massis3.stream.PositionUpdate;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class LaunchSimpleSimulationVerticle {

	private static final Logger log = LoggerFactory.getLogger(LaunchSimpleSimulationVerticle.class);
	static
	{
		Massis3ServiceUtils.configureVertxJSONMapper();
	}

	public static void main(String[] args)
	{
		Vertx vertx = Vertx.vertx(new VertxOptions().setEventLoopPoolSize(1).setWorkerPoolSize(1));

		SimulationServerConfig cfg = new SimulationServerConfig()
				.withAssetFolders(Arrays.asList(
						"/home/mcardenas/git-projects/massis3-assets/Scenes",
						"/home/mcardenas/git-projects/massis3-assets/models",
						"/home/mcardenas/git-projects/massis3-assets/animations"))
				.withHttpServerConfig(
						new HttpServerConfig().withHost("127.0.0.1").withPort(8082))
				.withAuthPropertiesFile("classpath:webauth.properties")
				.withInstances(1)
				.withRendererType(RendererType.LWJGL_OPEN_GL_3)
				.withRenderMode(RenderMode.DESKTOP);

		Future<String> launchFuture = Future.future();
		SimulationServerLauncher.launch(vertx, cfg, launchFuture.completer());

		launchFuture.mapEmpty()
				.compose(_void -> createSim(vertx, "Faculty_1floor"))
				//.compose(_void -> createSim(vertx, "Faculty_2floors"))
				//.compose(_void -> createSim(vertx, "BigSimpleSpace"))
				.setHandler(ar -> {
					if (ar.failed())
					{
						log.error("Failed to create simulations", ar.cause());
						System.exit(-1);

					} else
					{

						if (log.isInfoEnabled())
						{
							log.info("Simulations launched ok.");
						}
						printSimulations(vertx);
					}
				});

	}

	static AtomicLong simCounter = new AtomicLong(0);

	private static void printSimulations(Vertx vertx)
	{
		SimulationServerService proxy = Massis3ServiceUtils.createProxy(
				vertx,
				SimulationServerService.class,
				Massis3ServiceUtils.GLOBAL_SERVICE_GROUP);
		proxy.activeSimulations(ar -> {
			if (ar.failed())
			{
				log.error("Error when quering simulation data", ar.cause());
			} else
			{

				JsonArray simIds = ar.result();
				for (int i = 0; i < simIds.size(); i++)
				{
					final long simId = simIds.getLong(i);
					EnvironmentService es = Massis3ServiceUtils.createProxy(vertx,
							EnvironmentService.class, simId);

					Future<JsonArray> roomIds = Future.future();
					es.roomIds(roomIds.completer());

					Future<JsonArray> allRooms = Future.future();
					es.allRoomsInfo(allRooms.completer());

					Future<JsonArray> cameraIds = Future.future();
					es.cameraIds(cameraIds.completer());

					Future<String> sceneName = Future.future();
					es.sceneName(sceneName.completer());

					CompositeFuture.all(roomIds, allRooms, cameraIds, sceneName).setHandler(ar2 -> {
						if (ar2.failed())
						{
							log.error("Error when retrieving simulation data", ar.cause());
						} else
						{
							log.info("==================\n" +
									"Information about simulation: {}\n\t" +
									"RoomIds   {}\n\t" +
									"allRooms  {}\n\t" +
									"cameraIds {}\n\t" +
									"sceneName {}\n\t" +
									"==================",
									simId,
									roomIds.result(),
									allRooms.result(),
									cameraIds.result(),
									sceneName.result());
						}
					});
					testHuman1(vertx, simId, "walk1");
					testHuman1(vertx, simId, "run");
				}
			}
		});
	}

	private static Future<Long> createSim(Vertx vertx, String sceneFile)
	{
		SimulationServerService proxy = Massis3ServiceUtils.createProxy(
				vertx,
				SimulationServerService.class,
				Massis3ServiceUtils.GLOBAL_SERVICE_GROUP);
		Future<Long> simCreateFuture = Future.future();
		proxy.create(sceneFile, simCreateFuture.completer());
		return simCreateFuture;
	}

	private static void createHumanAndGoto(Vertx vertx, long simulationId, JsonPoint creationPoint,
			JsonPoint endPoint, String animation)
	{
		HumanAgentService agentService = Massis3ServiceUtils.createProxy(
				vertx,
				HumanAgentService.class,
				simulationId);
		agentService.createHuman(creationPoint, ar -> {
			if (ar.failed())
			{
				log.error("Human creation failed", ar.cause());
			} else
			{
				long humanId = ar.result();
				// X They run in parallel
				/* |__> */agentService.animate(humanId, animation, true);
				/* |__> */agentService.moveTowards(humanId, endPoint);

				// Arrival check
				Future<String> streamAddressF = Future.future();
				agentService.positionStreamingAddress(streamAddressF.completer());
				streamAddressF.map(addr -> {
					MessageConsumer<JsonObject> consumer = vertx.eventBus().consumer(addr);
					consumer.handler(msg -> {
						Vector3f position = msg.body().mapTo(PositionUpdate.class).getPosition();
						float distToTarget = position.distance(
								new Vector3f(
										endPoint.getX(),
										endPoint.getY(),
										endPoint.getZ()));
						if (distToTarget < 1f)
						{
							// agent has reached, for example
							agentService.stopMoving(humanId, ar2 -> {
							});
							agentService.animate(humanId, "wave", true);
							vertx.setTimer(3000,
									timerId -> agentService.animate(humanId, "die", true));
							vertx.setTimer(9000,
									timerId -> agentService.destroyHuman(humanId, ar2 -> {
									}));
							consumer.unregister();
						}
					});
					return null;
				});

			}
		});
	}

	private static void testHuman1(Vertx vertx, long simulationId, String animation)
	{
		//
		createHumanAndGoto(vertx, simulationId,
				new JsonPoint(51.971745f, 2.3800337f, 39.662235f),
				new JsonPoint(59.26255f, 0.41312274f, 19.185688f), animation);
	}

}
