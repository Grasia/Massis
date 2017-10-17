package com.massisframework.massis3.services.eventbus;

import com.massisframework.massis3.services.eventbus.annotations.SimulationServiceAddress;

import io.vertx.codegen.annotations.ProxyClose;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@ProxyGen
@VertxGen
@SimulationServiceAddress("echo")
/**
 * Service for testing the service proxy functionalities
 * @author rpax
 *
 */
public interface EchoService {

	public void echoString(String message, Handler<AsyncResult<String>> handler);

	public void echoInteger(Integer message, Handler<AsyncResult<Integer>> handler);

	public void echoJsonObject(JsonObject message, Handler<AsyncResult<JsonObject>> handler);

	public void echoJsonArray(JsonArray message, Handler<AsyncResult<JsonArray>> handler);

	public void echoVoidNoParams(Handler<AsyncResult<Void>> handler);

	public void echoVoidWithParams(String a, String b, Integer c,
			Handler<AsyncResult<Void>> handler);

	public void echoCanFail(boolean fail, String errMessage, Handler<AsyncResult<Boolean>> handler);
	
	public void echoException(Handler<AsyncResult<Void>> handler);
	
	public void echoCounterStreamAddress(Handler<AsyncResult<String>> handler);

	@ProxyClose
	public default void close() {}
}
