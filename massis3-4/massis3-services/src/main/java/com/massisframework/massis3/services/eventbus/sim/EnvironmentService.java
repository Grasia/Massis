package com.massisframework.massis3.services.eventbus.sim;

import com.massisframework.massis3.services.dataobjects.JsonPoint;
import com.massisframework.massis3.services.dataobjects.JsonQuaternion;
import com.massisframework.massis3.services.eventbus.annotations.IdempotentAction;
import com.massisframework.massis3.services.eventbus.annotations.InnerType;
import com.massisframework.massis3.services.eventbus.annotations.SimulationServiceAddress;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@ProxyGen
@VertxGen
@SimulationServiceAddress("environment-service")
public interface EnvironmentService {

	/**
	 * Retrieves the id of the areas (rooms) of the simulation environment
	 * 
	 * @param resultHandler
	 */
	@IdempotentAction
	public void roomIds(@InnerType(Long.class) Handler<AsyncResult<JsonArray>> resultHandler);

	@IdempotentAction
	public void allRoomsInfo(Handler<AsyncResult<JsonArray>> resultHandler);

	/**
	 * 
	 * @param id
	 *            the id og f the room
	 * @param resultHandler
	 *            the information of the room, following the schema:
	 * 
	 */
	@IdempotentAction
	public void roomInfo(long id, Handler<AsyncResult<JsonObject>> resultHandler);

	/**
	 * 
	 * @param name
	 *            the name of the entity to be searched
	 * @param resultHandler
	 *            A json array of the simulation entities that have the name
	 *            provided. The entities are returned as their id.
	 */
	@IdempotentAction
	public void entitiesNamed(String name, Handler<AsyncResult<JsonArray>> resultHandler);

	@GenIgnore
	public default Future<JsonArray> findEntitiesByName(String name)
	{
		Future<JsonArray> f = Future.future();
		this.entitiesNamed(name, f.completer());
		return f;
	}

	/**
	 * 
	 * @param id
	 *            the id of the entity
	 * @param resultHandler
	 *            the location of the entity
	 */
	@IdempotentAction
	public void entityLocation(long id, Handler<AsyncResult<JsonPoint>> resultHandler);

	@GenIgnore
	public default Future<JsonPoint> getEntityLocation(long id)
	{
		Future<JsonPoint> f = Future.future();
		this.entityLocation(id, f.completer());
		return f;
	}

	/**
	 * Retrieves the simulation time, in seconds. Note that the time returned
	 * might not be <i>real</i> time: can be faster or slower.
	 * 
	 * @param resultHandler
	 */
	@IdempotentAction
	public void time(Handler<AsyncResult<Float>> resultHandler);

	@IdempotentAction
	public void timeStreamAddress(Handler<AsyncResult<String>> resultHandler);

	/**
	 * Returns the name of the simulation scene
	 * 
	 * @param resultHandler
	 */
	@IdempotentAction
	public void sceneName(Handler<AsyncResult<String>> resultHandler);

	/**
	 * Attachs a camera to the simulation. Returns the camera id.
	 * 
	 * @param resultHandler
	 */
	public void addCamera(Handler<AsyncResult<String>> resultHandler);

	/**
	 * Moves a camera
	 * 
	 * @param resultHandler
	 */
	public void setCameraLocation(
			String camId, JsonPoint location,
			Handler<AsyncResult<Void>> resultHandler);

	/**
	 * Moves a camera
	 * 
	 * @param resultHandler
	 */
	public void setCameraRotation(
			String camId, JsonQuaternion rotation,
			Handler<AsyncResult<Void>> resultHandler);

	public void setCameraRotationWithAngles(
			String camId, float xAngle, float yAngle, float zAngle,
			Handler<AsyncResult<Void>> resultHandler);

	public void cameraIds(@InnerType(String.class) Handler<AsyncResult<JsonArray>> resultHandler);

	/**
	 * Returns a key-value pair of the local map holding the camera data image
	 * data as jpg.
	 * 
	 * 
	 * @param resultHandler
	 */
	@IdempotentAction
	public void cameraMapKeyValue(String camId,
			@InnerType(String.class) Handler<AsyncResult<JsonArray>> resultHandler);
	
	@IdempotentAction
	public void cameraDataStreamEventBusAddress(String camId,Handler<AsyncResult<String>> handler);

}
