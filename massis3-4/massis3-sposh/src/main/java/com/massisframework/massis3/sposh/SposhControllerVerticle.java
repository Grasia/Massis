package com.massisframework.massis3.sposh;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.math.Vector3f;
import com.massisframework.massis3.services.dataobjects.JsonPoint;
import com.massisframework.massis3.services.eventbus.Massis3ServiceUtils;
import com.massisframework.massis3.services.eventbus.sim.EnvironmentService;
import com.massisframework.massis3.services.eventbus.sim.HumanAgentService;
import com.massisframework.massis3.sposh.library.PlanParserUtils;

import cz.cuni.amis.pogamut.sposh.elements.PoshPlan;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class SposhControllerVerticle extends AbstractVerticle implements Massis3Agent {

	private static final Logger log = LoggerFactory.getLogger(SposhControllerVerticle.class);
	private SposhLogicController controller;
	private HumanAgentService agentService;
	private EnvironmentService environmentService;
	private String poshPlanURL;
	private Long agentId;
	private Vector3f position;
	private Float time;
	private long simId = 0;

	@Override
	public void start(Future<Void> startFuture) throws Exception
	{
		this.simId = config().getLong("simId");
		this.agentId = config().getLong("agentId");
		this.poshPlanURL = config().getString("planurl");
		this.position = new Vector3f();
		this.agentService = Massis3ServiceUtils.createProxy(vertx, HumanAgentService.class,
				String.valueOf(simId));
		this.environmentService = Massis3ServiceUtils.createProxy(vertx, EnvironmentService.class,
				String.valueOf(simId));

		this.agentService.humanExists(this.agentId, res -> {
			if (res.failed())
			{
				startFuture.fail(res.cause());
				return;
			} else
			{
				boolean exists = res.result();
				if (!exists)
				{
					log.error("human with id {} does not exist.Exiting", this.agentId);
					startFuture.fail("Human entity [" + this.agentId + "] does not exist");
					return;
				} else
				{

					if (log.isInfoEnabled())
					{
						log.info("Retrieving initial position");
					}
					this.positionLoop(_void -> {
						try
						{

							if (log.isInfoEnabled())
							{
								log.info("Starting behavior");
							}

							startBehavior();
						} catch (MalformedURLException e)
						{
							startFuture.fail(e);
							return;
						}
						if (log.isInfoEnabled())
						{
							log.info("Behavior started");
						}
						startFuture.complete();
					});

					// retrieve position

				}
			}
		});

	}

	private void startBehavior() throws MalformedURLException
	{

		PoshPlan poshPlan = PlanParserUtils.parsePlan(new URL(poshPlanURL));
		Massis3Context<Massis3Agent> ctx = new Massis3Context<>(this);
		this.controller = new SposhLogicController(ctx, poshPlan);
		addTimer(100, this::logicLoop);
		addTimer(100, this::positionLoop);
		// FIXME call time initialization before loop
		addTimer(100, this::timeLoop);
	}

	private void logicLoop(Handler<Void> handler)
	{

		this.controller.logic();
		handler.handle(null);
	}

	private void timeLoop(Handler<Void> handler)
	{
		this.environmentService.time(r -> {
			if (r.failed())
			{
				log.error("Error when retrieving time", r.cause());
				handler.handle(null);
			} else
			{
				this.time = r.result();
				handler.handle(null);
			}
		});
	}

	private void addTimer(long delay, Consumer<Handler<Void>> action)
	{
		final AtomicBoolean active = new AtomicBoolean(false);
		this.vertx.setPeriodic(delay, timerId -> {

			if (active.getAndSet(true))
			{
				return;
			}
			action.accept(r -> {
				active.set(false);
			});
		});
	}

	private void positionLoop(Handler<Void> finishHandler)
	{
		this.agentService.getLocation(this.agentId, r -> {
			if (r.failed())
			{
				log.error("Error when retrieving position", r.cause());
				finishHandler.handle(null);
			} else
			{

				JsonPoint result = r.result();
				this.position.set(result.getX(), result.getY(), result.getZ());
				finishHandler.handle(null);
			}
		});
	}

	@Override
	public void stop() throws Exception
	{
		this.controller.cleanup();
	}

	@Override
	public long getID()
	{
		return this.agentId;
	}

	@Override
	public void animate(String animation, boolean loop, Handler<AsyncResult<Void>> handler)
	{
		this.agentService.animate(getID(), animation, loop, handler);
	}

	@Override
	public void moveTowards(Vector3f t, Handler<AsyncResult<Void>> handler)
	{
		this.agentService.moveTowards(getID(), new JsonPoint(t.x, t.y, t.z), handler);
	}

	@Override
	public void findEntitiesByName(String name, Handler<AsyncResult<JsonArray>> handler)
	{
		this.environmentService.entitiesNamed(name, handler);
	}

	@Override
	public void isFollowingPath(Handler<AsyncResult<Boolean>> handler)
	{
		this.agentService.stopMoving(getID(), handler);
	}

	@Override
	public void stopMoving(Handler<AsyncResult<Boolean>> handler)
	{
		this.agentService.stopMoving(getID(), handler);
	}

	@Override
	public float getTime()
	{
		return this.time;
	}

	@Override
	public Vector3f getPosition()
	{
		return this.position;
	}

	@Override
	public void getSceneRooms(Handler<AsyncResult<JsonArray>> handler)
	{
		this.environmentService.allRoomsInfo(handler);
	}

	@Override
	public void getRoomInfo(long id, Handler<AsyncResult<JsonObject>> resultHandler)
	{
		this.environmentService.roomInfo(id, resultHandler);
	}

	private Future<JsonObject> getRoomInfo(long id)
	{
		Future<JsonObject> f = Future.future();
		this.environmentService.roomInfo(id, f.completer());
		return f;
	}

}
