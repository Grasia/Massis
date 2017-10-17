package com.massisframework.massis3.services.eventbus.sim;

import com.massisframework.massis3.services.eventbus.annotations.SimulationServiceAddress;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;

@ProxyGen
@VertxGen
@SimulationServiceAddress("systems-service")
/**
 * 
 * @author rpax
 *
 */
public interface SystemsService {

	public void getRunningSystems(Handler<AsyncResult<JsonArray>> resultHandler);
	public void systemStatus(String systemName, Handler<AsyncResult<Boolean>> resultHandler);
	public void disableSystem(String systemName, Handler<AsyncResult<Boolean>> resultHandler);
	public void enableSystem(String systemName, Handler<AsyncResult<Boolean>> resultHandler);

}
