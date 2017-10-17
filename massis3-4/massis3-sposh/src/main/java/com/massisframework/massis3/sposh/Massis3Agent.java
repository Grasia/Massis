package com.massisframework.massis3.sposh;

import com.jme3.math.Vector3f;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public interface Massis3Agent {

	public long getID();

	public void animate(String animation, boolean loop, Handler<AsyncResult<Void>> resultHandler);

	public void moveTowards(Vector3f target, Handler<AsyncResult<Void>> resultHandler);

	public void findEntitiesByName(String name, Handler<AsyncResult<JsonArray>> handler);
	
	public void getSceneRooms(Handler<AsyncResult<JsonArray>> handler);

	public void isFollowingPath(Handler<AsyncResult<Boolean>> handler);

	public void stopMoving(Handler<AsyncResult<Boolean>> handler);

	public float getTime();

	public Vector3f getPosition();

	void getRoomInfo(long id, Handler<AsyncResult<JsonObject>> resultHandler);

	
}
