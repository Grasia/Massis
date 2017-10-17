package com.massisframework.massis3.services.http.controllers;

import java.util.List;
import java.util.function.Function;

import com.github.aesteve.vertx.nubes.annotations.mixins.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.aesteve.vertx.nubes.annotations.Controller;
import com.github.aesteve.vertx.nubes.annotations.params.Param;
import com.github.aesteve.vertx.nubes.annotations.params.RequestBody;
import com.github.aesteve.vertx.nubes.annotations.routing.http.GET;
import com.github.aesteve.vertx.nubes.annotations.routing.http.POST;
import com.massisframework.massis3.web.response.MassisAPIResponse;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;

@Controller("/services")
@ContentType({ "application/json", "*/*" })
public class ServicesController {

	private static final Logger log = LoggerFactory.getLogger(ServicesController.class);
	

	@GET("/")
	public void getRecords(
			ServiceDiscovery discovery,
			MassisAPIResponse response)
	{
		
		if (log.isDebugEnabled())
		{
			log.debug("Retrieving records");
		}

		Future<List<Record>> f = Future.future();
		discovery.getRecords(record -> true, true, f.completer());
		f.map(records -> {
			JsonArray result = new JsonArray();
			records.stream().map(r -> r.toJson()).forEach(result::add);
			return result;
		}).setHandler(response.handler());
	}

	@POST("/:endpoint/:action")
	//@ContentType("application/json")
	public void handleServiceCall(
			@Param(value = "endpoint", mandatory = true) String endpoint,
			@Param(value = "action", mandatory = true) String action,
			@RequestBody JsonObject requestData,
			Vertx vertx,
			ServiceDiscovery discovery,
			MassisAPIResponse response)
	{

		
		if (log.isDebugEnabled())
		{
			log.debug("Calling service : {}",response.getRoutingContext().request().absoluteURI());
		}

		JsonObject _json = requestData.copy();

		Function<Record, Boolean> recordFilter = record -> record
				.getLocation()
				.getString("endpoint")
				.equals(endpoint);

		discovery.getRecord(recordFilter, true, ar -> {
			if (ar.failed())
			{
				response.handle(ar);
				return;
			} else
			{

				Record record = ar.result();
				if (record == null)
				{
					response.writeJsonError(404,
							"No service found. Is the endpoint correctly typed?", true);
					return;
				}

				JsonObject actions = record.getMetadata().getJsonObject("actions");
				if (actions == null)
				{
					response.writeJsonError(400, "This service can't be called from http", true);

				} else if (!actions.containsKey(action))
				{
					response.writeJsonError(400,
							"Action " + action + " is not offered by the service. Offered Actions: "
									+ actions.fieldNames(),
							true);
				} else
				{
					DeliveryOptions _deliveryOptions = new DeliveryOptions();
					_deliveryOptions.addHeader("action", action);
					vertx.eventBus().send(endpoint, _json, _deliveryOptions, response.handler());
				}
			}
		});
	}
}
