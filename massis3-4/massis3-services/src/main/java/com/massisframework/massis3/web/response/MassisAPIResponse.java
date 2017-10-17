package com.massisframework.massis3.web.response;

import com.github.aesteve.vertx.nubes.reflections.injectors.UsesRoutingContext;
import com.massisframework.massis3.web.response.impl.MassisAPIResponseImpl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

@UsesRoutingContext
public interface MassisAPIResponse {

	public static MassisAPIResponse create(RoutingContext ctx)
	{
		return new MassisAPIResponseImpl(ctx);
	}

	<T> Handler<AsyncResult<T>> handler();

	void writeJsonOK(Object result, boolean pretty);

	void writeJsonError(int errCode, String errorMsg, boolean pretty);

	<T> void handle(AsyncResult<T> r);

	public RoutingContext getRoutingContext();

}