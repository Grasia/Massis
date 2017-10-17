package com.massisframework.massis3.services.eventbus;

import com.massisframework.massis3.services.eventbus.annotations.IdempotentAction;
import com.massisframework.massis3.services.eventbus.annotations.SimulationServiceAddress;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;

@ProxyGen
@VertxGen
@SimulationServiceAddress("simulation-server")
public interface SimulationServerService {
	/**
	 * Retrieves the current active simulations
	 * 
	 * @param resultHandler
	 */
	@IdempotentAction
	public void activeSimulations(Handler<AsyncResult<JsonArray>> resultHandler);

	/**
	 * Retrieves the current available simulation scenes
	 * 
	 * @param resultHandler
	 */
	@IdempotentAction
	public void availableScenes(Handler<AsyncResult<JsonArray>> resultHandler);

	/**
	 * Creates a simulation
	 * 
	 * @param sceneFile
	 *            the simulation scene file
	 * @param resultHandler
	 */
	public void create(String sceneFile, Handler<AsyncResult<Long>> resultHandler);

	/**
	 * Destroys (stops and undeploy) a running simulation)
	 * 
	 * @param simId
	 *            the simulation id
	 * @param resultHandler
	 */
	public void destroy(long simId, Handler<AsyncResult<Void>> resultHandler);

}
