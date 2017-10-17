package com.massisframework.massis3.web.response.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.aesteve.vertx.nubes.reflections.injectors.UsesRoutingContext;
import com.massisframework.massis3.services.Responses;
import com.massisframework.massis3.web.response.MassisAPIResponse;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

@UsesRoutingContext
public class MassisAPIResponseImpl implements MassisAPIResponse {

	private static final Logger log = LoggerFactory.getLogger(MassisAPIResponseImpl.class);
	private HttpServerResponse response;
	private RoutingContext ctx;

	public MassisAPIResponseImpl(RoutingContext ctx)
	{
		this.ctx = ctx;
		this.response = ctx.response();
		response.setChunked(true);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.massisframework.massis3.simulation.server.http.impl.MassisAPIResponse
	 * #handler()
	 */
	@Override
	public <T> Handler<AsyncResult<T>> handler()
	{
		return this::handle;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.massisframework.massis3.simulation.server.http.impl.MassisAPIResponse
	 * #writeJsonOK(java.lang.Object, boolean)
	 */
	@Override
	public void writeJsonOK(final Object result, boolean pretty)
	{
		JsonObject json = Responses.jsonResponseOK(result);
		response.setStatusCode(200).end(pretty ? json.encodePrettily() : json.encode());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.massisframework.massis3.simulation.server.http.impl.MassisAPIResponse
	 * #writeJsonError(int, java.lang.String, boolean)
	 */
	@Override
	public void writeJsonError(final int errCode, String errorMsg, boolean pretty)
	{
		JsonObject json = Responses.jsonResponseError(errCode, errorMsg);
		response.setStatusCode(errCode).end(pretty ? json.encodePrettily() : json.encode());
	}

	@Override
	public <T> void handle(AsyncResult<T> result)
	{
		if (result.failed())
		{
			log.error("Error when executing http operation", result.cause());
			writeJsonError(500, result.cause().getMessage(), true);

		} else
		{
			writeJsonOK(result.result(), true);
		}
	}

	@Override
	public RoutingContext getRoutingContext()
	{
		return this.ctx;
	}

}
