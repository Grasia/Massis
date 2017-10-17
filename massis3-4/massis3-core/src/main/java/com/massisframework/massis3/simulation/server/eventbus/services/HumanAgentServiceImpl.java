package com.massisframework.massis3.simulation.server.eventbus.services;

import static com.massisframework.massis3.simulation.server.eventbus.services.CompletionFutures.fromCompletionStage;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.massisframework.massis3.commons.app.server.AppSystemManager;
import com.massisframework.massis3.core.components.Human.Age;
import com.massisframework.massis3.core.components.Human.Gender;
import com.massisframework.massis3.core.systems.control.human.HumanControlSystem;
import com.massisframework.massis3.core.systems.required.EntityDataSystem;
import com.massisframework.massis3.core.components.NameComponent;
import com.massisframework.massis3.core.components.RoomComponent;
import com.massisframework.massis3.services.dataobjects.JsonPoint;
import com.massisframework.massis3.services.eventbus.sim.HumanAgentService;
import com.massisframework.massis3.simulation.ecs.EntityComponentAccessor;
import com.massisframework.massis3.stream.PositionUpdate;
import com.simsilica.es.EntityId;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;

public class HumanAgentServiceImpl extends AbstractVerticle implements HumanAgentService {

	private static final Logger log = LoggerFactory.getLogger(HumanAgentServiceImpl.class);
	private HumanControlSystem humanControl;
	private EntityComponentAccessor eqs;
	private String positionAddr;

	public HumanAgentServiceImpl(AppSystemManager stateManager)
	{
		this.humanControl = stateManager.getSystem(HumanControlSystem.class);
		EntityDataSystem edAppState = stateManager.getSystem(EntityDataSystem.class);
		this.eqs = edAppState.createAbsoluteAccessor();
	}

	@Override
	public void start() throws Exception
	{
		this.positionAddr = UUID.randomUUID().toString();

		this.vertx.setPeriodic(100, tId -> {
			this.humanControl.getHumanIds().forEach(eid -> {
				final long time = (long) (humanControl.time() * 1000L);
				Vector3f location = humanControl.getLocation(eid);
				PositionUpdate update = new PositionUpdate(eid, location, time);
				vertx.eventBus().publish(this.positionAddr, update.toJson());
			});
		});
	}

	@Override
	public void stop() throws Exception
	{

	}

	@Override
	public void animate(
			long humanId,
			String animationName,
			boolean loop,
			Handler<AsyncResult<Void>> resultHandler)
	{
		CompletionStage<EntityId> res = humanControl.animateHuman(humanId, animationName);
		resultHandler.handle(fromCompletionStage(res).mapEmpty());
	}

	@Override
	public void createHuman(JsonPoint jsonLoc, Handler<AsyncResult<Long>> resultHandler)
	{
		if (jsonLoc == null)
		{
			resultHandler.handle(Future.failedFuture("location cannot be null"));
			return;
		}

		CompletionStage<EntityId> cs = humanControl.createHuman(Gender.FEMALE, Age.NORMAL,
				fromPoint(jsonLoc));
		resultHandler.handle(fromCompletionStage(cs).map(eid -> eid.getId()));

	}

	@Override
	public void getLocation(long humanId, Handler<AsyncResult<JsonPoint>> resultHandler)
	{
		try
		{
			Vector3f res = humanControl.getLocation(humanId);
			resultHandler.handle(Future.succeededFuture(fromVector3f(res)));
		} catch (Exception e)
		{
			resultHandler.handle(Future.failedFuture(e));
		}
	}

	private static JsonPoint fromVector3f(Vector3f v)
	{
		return new JsonPoint(v.x, v.y, v.z);
	}

	private static Vector3f fromPoint(JsonPoint v)
	{
		return new Vector3f(v.getX(), v.getY(), v.getZ());
	}

	@Override
	public void isFollowingPath(long humanId, Handler<AsyncResult<Boolean>> resultHandler)
	{
		CompletionStage<Boolean> res = this.humanControl.isFollowingPath(humanId);
		resultHandler.handle(fromCompletionStage(res));
	}

	@Override
	public void moveTowards(long humanId, JsonPoint target, Handler<AsyncResult<Void>> result)
	{
		CompletionStage<Boolean> res = this.humanControl.moveTo(humanId, fromPoint(target));
		result.handle(fromCompletionStage(res).mapEmpty());

	}

	@Override
	public void stopMoving(long humanId, Handler<AsyncResult<Boolean>> result)
	{
		CompletionStage<Boolean> res = this.humanControl.stopMoving(humanId);
		result.handle(fromCompletionStage(res));
	}

	@Override
	public void getHumanIdsInRange(long humanId, float range,
			Handler<AsyncResult<JsonArray>> result)
	{
		CompletionStage<List<Long>> res = this.humanControl.getHumansInRange(humanId, range);
		result.handle(fromCompletionStage(res).map(l -> new JsonArray(l)));
	}

	@Override
	public void positionStreamingAddress(Handler<AsyncResult<String>> result)
	{
		result.handle(Future.succeededFuture(positionAddr));
	}

	@Override
	public void humanExists(long humanId, Handler<AsyncResult<Boolean>> result)
	{
		result.handle(Future.succeededFuture(this.humanControl.isHuman(humanId)));
	}

	@Override
	public void createHumanInArea(String name, Handler<AsyncResult<Long>> resultHandler)
	{
		// 1. Find rooms matching name
		RoomComponent room = this.eqs.findEntities(null, RoomComponent.class, NameComponent.class)
				.stream()
				.filter(eid -> name.equals(eqs.get(eid, NameComponent.class).getName()))
				.map(eid -> eqs.get(eid, RoomComponent.class))
				.findFirst()
				.orElse(null);
		if (room == null)
		{
			resultHandler.handle(Future.failedFuture("There is no area named " + name));
			return;
		}
		Vector3f rndPoint = randomPointInPolygon(room.getPoints());
		this.createHuman(fromVector3f(rndPoint), resultHandler);
		// find random point in room
	}

	// TODO duplicated code!
	private static Vector3f randomPointInPolygon(List<Vector3f> points)
	{
		Vector3f min = new Vector3f();
		Vector3f max = new Vector3f();
		points.stream().forEach(point -> {
			min.x = Math.min(point.x, min.x);
			min.y = Math.min(point.y, min.y);
			min.z = Math.min(point.z, min.z);

			max.x = Math.max(point.x, max.x);
			max.y = Math.max(point.y, max.y);
			max.z = Math.max(point.z, max.z);
		});

		float x, z;
		do
		{
			x = min.x + (max.x - min.x) * FastMath.nextRandomFloat();
			z = min.z + (max.z - min.z) * FastMath.nextRandomFloat();
		} while (!pointInPolygon(points, x, z));

		return new Vector3f(x, min.y, z);

	}

	// https://stackoverflow.com/a/8721483/3315914
	public static boolean pointInPolygon(List<Vector3f> points, float x, float z)
	{
		int i;
		int j;
		boolean result = false;

		for (i = 0, j = points.size() - 1; i < points.size(); j = i++)
		{
			if ((points.get(i).z > z) != (points.get(j).z > z) &&
					(x < (points.get(j).x
							- points.get(i).x)
							* (z - points.get(i).z)
							/ (points.get(j).z
									- points.get(i).z)
							+ points.get(i).x))
			{
				result = !result;
			}
		}
		return result;
	}

	@Override
	public void humanIds(Handler<AsyncResult<JsonArray>> resultHandler)
	{
		JsonArray res = new JsonArray();
		humanControl.getHumanIds().stream().map(EntityId::getId).forEach(res::add);
		resultHandler.handle(Future.succeededFuture(res));
	}

	@Override
	public void destroyHuman(Long humanId, Handler<AsyncResult<Void>> resultHandler)
	{
		this.humanControl.destroyHuman(humanId);
		resultHandler.handle(Future.succeededFuture());
	}
}
