package com.massisframework.massis3.services.eventbus.impl;

import java.util.concurrent.atomic.AtomicInteger;

import com.massisframework.massis3.services.eventbus.EchoService;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class EchoServiceImpl implements EchoService {

	private static String ECHO_STREAM_ADDR = "m3_echoServiceStream";
	private AtomicInteger count = new AtomicInteger();

	public EchoServiceImpl(Vertx vertx)
	{
		vertx.setPeriodic(1000, tId -> {
			vertx.eventBus().publish(ECHO_STREAM_ADDR, String.valueOf(count.getAndIncrement()));
		});
	}

	@Override
	public void echoString(String message, Handler<AsyncResult<String>> handler)
	{
		echoInternal(message, handler);
	}

	private <T> void echoInternal(T message, Handler<AsyncResult<T>> handler)
	{
		handler.handle(Future.succeededFuture(message));
	}

	@Override
	public void echoInteger(Integer message, Handler<AsyncResult<Integer>> handler)
	{
		echoInternal(message, handler);
	}

	@Override
	public void echoJsonObject(JsonObject message, Handler<AsyncResult<JsonObject>> handler)
	{
		echoInternal(message, handler);
	}

	@Override
	public void echoJsonArray(JsonArray message, Handler<AsyncResult<JsonArray>> handler)
	{
		echoInternal(message, handler);
	}

	@Override
	public void echoVoidNoParams(Handler<AsyncResult<Void>> handler)
	{
		handler.handle(Future.succeededFuture());
	}

	@Override
	public void echoVoidWithParams(
			String a, String b, Integer c,
			Handler<AsyncResult<Void>> handler)
	{
		handler.handle(Future.succeededFuture());
	}

	@Override
	public void echoCanFail(boolean fail, String errMessage, Handler<AsyncResult<Boolean>> handler)
	{
		if (fail)
		{
			handler.handle(Future.failedFuture(errMessage));
		} else
		{
			handler.handle(Future.succeededFuture(true));
		}

	}

	@Override
	public void echoException(Handler<AsyncResult<Void>> handler)
	{
		handler.handle(Future.failedFuture(new RuntimeException("this is a RuntimeException!")));

	}

	@Override
	public void echoCounterStreamAddress(Handler<AsyncResult<String>> handler)
	{
		handler.handle(Future.succeededFuture(ECHO_STREAM_ADDR));
	}


}
