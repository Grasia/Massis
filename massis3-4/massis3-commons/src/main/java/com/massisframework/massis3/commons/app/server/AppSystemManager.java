package com.massisframework.massis3.commons.app.server;

import java.util.List;
import java.util.Set;

import com.jme3.asset.AssetManager;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public interface AppSystemManager {

	public float tpf();

	public float time();

	public <T extends MassisSystem> T getSystem(Class<T> stateClass);

	public void enqueue(Runnable r);

	public AssetManager getDefaultAssetManager();

	void initialize(Handler<AsyncResult<Void>> handler);

	public void createCamera(int width, int height, Handler<AsyncResult<ServerCamera>> handler);

	public void shutdown();

	public void releaseCamera(ServerCamera cam, Handler<AsyncResult<Void>> handler);

	public List<ServerCamera> getCameras();

	public void start(Handler<AsyncResult<Void>> handler);

	public Set<Class<? extends MassisSystem>> getRunningSystems();

	public void setSystemEnabled(
			Class<? extends MassisSystem> systemType,
			boolean enabled,
			Handler<AsyncResult<Boolean>> handler);

}
