package com.massisframework.massis3.simulation.server.eventbus.services;

import com.massisframework.massis3.commons.app.server.AppSystemManager;
import com.massisframework.massis3.commons.app.server.MassisSystem;
import com.massisframework.massis3.services.eventbus.sim.SystemsService;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;

public class SystemsServiceImpl extends AbstractVerticle implements SystemsService {

	private AppSystemManager systemsManager;

	public SystemsServiceImpl(AppSystemManager systemsManager)
	{
		this.systemsManager = systemsManager;
	}

	@Override
	public void getRunningSystems(Handler<AsyncResult<JsonArray>> resultHandler)
	{
		JsonArray res = new JsonArray();
		systemsManager.getRunningSystems().stream().map(s -> s.getName()).forEach(res::add);
		resultHandler.handle(Future.succeededFuture(res));
	}

	@Override
	public void systemStatus(String systemName, Handler<AsyncResult<Boolean>> handler)
	{
		// 1. Check if the provided system name matches
		MassisSystem system = getSystemByName(systemName);
		if (system == null)
		{
			handler.handle(Future.failedFuture("System with name " + systemName + " not found."));
		} else
		{
			handler.handle(Future.succeededFuture(system.isEnabled()));
		}
	}

	private MassisSystem getSystemByName(String systemName)
	{
		return systemsManager.getRunningSystems()
				.stream()
				.filter(s -> s.getName().endsWith(systemName))
				.map(systemsManager::getSystem)
				.findAny()
				.orElse(null);
	}

	@Override
	public void disableSystem(String systemName, Handler<AsyncResult<Boolean>> handler)
	{
		enableSystem(systemName, false, handler);
	}

	@Override
	public void enableSystem(String systemName, Handler<AsyncResult<Boolean>> handler)
	{
		enableSystem(systemName, true, handler);
	}

	private void enableSystem(String systemName, boolean enabled,
			Handler<AsyncResult<Boolean>> handler)
	{
		MassisSystem system = getSystemByName(systemName);
		if (system == null)
		{
			handler.handle(Future.failedFuture("System with name " + systemName + " not found."));
		} else
		{
			this.systemsManager.setSystemEnabled(system.getClass(), enabled, handler);
		}
	}

}
