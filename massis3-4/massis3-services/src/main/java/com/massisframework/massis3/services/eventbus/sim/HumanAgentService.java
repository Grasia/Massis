package com.massisframework.massis3.services.eventbus.sim;

import com.massisframework.massis3.services.dataobjects.JsonPoint;
import com.massisframework.massis3.services.eventbus.annotations.IdempotentAction;
import com.massisframework.massis3.services.eventbus.annotations.InnerType;
import com.massisframework.massis3.services.eventbus.annotations.SimulationServiceAddress;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;

@ProxyGen
@VertxGen
@SimulationServiceAddress("human-agent-service")
public interface HumanAgentService {

	/**
	 * Creates a human in the specified location [x,y,z]
	 * 
	 * @param location
	 * @param resultHandler
	 */
	public void createHuman(JsonPoint location, Handler<AsyncResult<Long>> resultHandler);
	public void destroyHuman(Long humanId,Handler<AsyncResult<Void>> resultHandler);

	/**
	 * Returns the human ids of the simulation
	 * 
	 * @param resultHandler
	 */
	@IdempotentAction
	public void humanIds(@InnerType(Long.class) Handler<AsyncResult<JsonArray>> resultHandler);

	public void createHumanInArea(String name, Handler<AsyncResult<Long>> resultHandler);

	@GenIgnore
	public default Future<Long> createHuman(JsonPoint location)
	{
		Future<Long> f = Future.future();
		createHuman(location, f.completer());
		return f;
	}

	/**
	 * Sets an animation to a human character. Note that the animation will be
	 * performed in loop mode.
	 * 
	 * @param humanId
	 *            the id of the human character
	 * @param animationName
	 *            the name of the animation. The extension {@code .massisanim}
	 *            is not needed.
	 * @param resultHandler
	 *            result handler that will be called when the operation is
	 *            completed.
	 * @param loop
	 *            if the animation should be executed in loop mode. If the value
	 *            provided is {@code true}, the execution will return
	 *            inmediately. Otherwise, this method will end once the
	 *            animation cycle has ended.
	 */
	public void animate(
			long humanId,
			String animationName,
			boolean loop,
			Handler<AsyncResult<Void>> resultHandler);

	@GenIgnore
	public default Future<Long> animate(long humanId, String animationName, boolean loop)
	{
		Future<Void> f = Future.future();
		animate(humanId, animationName, loop, f.completer());
		return f.map(humanId);
	}

	/**
	 * Retrieves the location of a human entity
	 * 
	 * @param humanId
	 *            the entity id of the human
	 * @param resultHandler
	 *            result handler that will be called when the operation is
	 *            complete.
	 */
	@IdempotentAction
	public void getLocation(long humanId, Handler<AsyncResult<JsonPoint>> resultHandler);

	/**
	 * Checks if the human is following a path FIXME seems not to be working
	 * properly
	 * 
	 * @param humanId
	 * @param resultHandler
	 */
	public void isFollowingPath(long humanId, Handler<AsyncResult<Boolean>> resultHandler);

	/**
	 * Tells the agent to move towards a point. Executes a pathfinding algorithm
	 * and follows a path
	 * 
	 * @param humanId
	 *            the id of the human
	 * @param target
	 *            the target to follow, with three coordinates.
	 * @param result
	 */
	public void moveTowards(long humanId, JsonPoint target, Handler<AsyncResult<Void>> result);

	@GenIgnore
	public default Future<Long> moveTowards(long humanId, JsonPoint target)
	{
		Future<Void> f = Future.future();
		moveTowards(humanId, target, f.completer());
		return f.map(humanId);
	}

	/**
	 * Forces the agent to stop moving, if it was moving.
	 * 
	 * @param humanId
	 *            the id of the agent
	 * @param result
	 */
	public void stopMoving(long humanId, Handler<AsyncResult<Boolean>> result);

	/**
	 * Retrieves the agents in range.
	 * 
	 * @param humanId
	 * @param range
	 * @param result
	 */
	@IdempotentAction
	public void getHumanIdsInRange(
			long humanId,
			float range,
			@InnerType(Long.class) Handler<AsyncResult<JsonArray>> result);

	@IdempotentAction
	public void positionStreamingAddress(Handler<AsyncResult<String>> result);

	@IdempotentAction
	public void humanExists(long humanId, Handler<AsyncResult<Boolean>> result);

}
