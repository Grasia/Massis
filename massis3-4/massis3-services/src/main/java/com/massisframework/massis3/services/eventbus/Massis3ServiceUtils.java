package com.massisframework.massis3.services.eventbus;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.massisframework.massis3.services.eventbus.annotations.SimulationServiceAddress;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.serviceproxy.ProxyHelper;

public interface Massis3ServiceUtils {

	public static final String GLOBAL_SERVICE_GROUP = "global";
	public static final String GROUP_KEY = "service.group";
	public static final String EVENT_TYPE_KEY = "m3_eventType";

	public static <T> String globalServiceAddress(Class<T> clazz)
	{
		return defaultSimulationServiceAddress(clazz, GLOBAL_SERVICE_GROUP);
	}

	public static <T> String defaultSimulationServiceAddress(Class<T> clazz, String serviceGroup)
	{
		Objects.requireNonNull(serviceGroup);
		SimulationServiceAddress simServiceAnn = clazz
				.getAnnotation(SimulationServiceAddress.class);

		if (simServiceAnn == null)
		{
			throw new UnsupportedOperationException(
					"Class must be annotated either with @"
							+ SimulationServiceAddress.class.getName());
		}
		String path = simServiceAnn.value();
		path += "." + serviceGroup;
		return path;

	}

	public static void configureVertxJSONMapper()
	{
		Json.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Json.prettyMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public static <T> T createProxy(Vertx vertx, Class<T> clazz, long id)
	{
		return createProxy(vertx, clazz, String.valueOf(id));
	}

	public static <T> T createProxy(Vertx vertx, Class<T> clazz, String id)
	{
		return ProxyHelper.createProxy(clazz, vertx, defaultSimulationServiceAddress(clazz, id));
	}

	public static <T> MessageConsumer<JsonObject> registerDefault(
			Vertx vertx,
			Class<T> clazz,
			String serviceGroup,
			T service)
	{
		String defAddr = defaultSimulationServiceAddress(clazz, serviceGroup);
		return ProxyHelper.registerService(clazz, vertx, service, defAddr);
	}

	public static <T> MessageConsumer<JsonObject> registerDefaultAndPublishRecord(
			Vertx vertx,
			Class<T> clazz,
			String serviceGroup,
			T service)
	{
		return registerDefaultAndPublishRecord(vertx, clazz, serviceGroup, service, r -> {
		});
	}

	public static <T> MessageConsumer<JsonObject> registerDefaultAndPublishRecord(
			Vertx vertx,
			Class<T> clazz,
			String serviceGroup,
			T service,
			Handler<AsyncResult<Record>> handler)
	{
		MessageConsumer<JsonObject> consumer = registerDefault(vertx, clazz, serviceGroup, service);
		publishService(vertx, clazz, serviceGroup, handler);
		return consumer;
	}

	public static <T> Record createServiceRecord(Class<T> clazz, String group)
	{
		String resourceName = clazz.getSimpleName() + ".json";
		String endpoint = defaultSimulationServiceAddress(clazz, group);
		try (InputStream is = clazz.getResourceAsStream(resourceName))
		{
			JsonObject json = new JsonObject(IOUtils.toString(is, "UTF-8"));
			Record r = new Record(json);
			r.getMetadata().put(GROUP_KEY, group);
			r.setLocation(new JsonObject().put("endpoint", endpoint));
			return r;
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static <T> void publishService(
			Vertx vertx,
			Class<T> clazz, String group,
			Handler<AsyncResult<Record>> handler)
	{
		ServiceDiscovery discovery = ServiceDiscovery.create(vertx);
		Record record = null;
		try
		{
			record = createServiceRecord(clazz, group);
		} catch (Exception e)
		{
			handler.handle(Future.failedFuture(e));
			return;
		}
		discovery.publish(record, ar -> {
			if (ar.succeeded())
			{
				// publication succeeded
				Record publishedRecord = ar.result();
				handler.handle(Future.succeededFuture(publishedRecord));

			} else
			{
				handler.handle(Future.failedFuture(ar.cause()));
			}
			discovery.close();
		});
	}

	public static JsonArray jsonArray(Object... values)
	{
		JsonArray arr = new JsonArray();
		for (int i = 0; i < values.length; i++)
		{
			arr.add(values[i]);
		}
		return arr;
	}
}
