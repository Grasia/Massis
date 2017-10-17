package com.massisframework.massis3.services.term.commands;

import static com.massisframework.massis3.services.term.VertxCommandHelper.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.massisframework.massis3.services.term.commands.AutoComplete.AutoCompleteCallback;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.shell.cli.Completion;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;

public class ServiceActionAutoComplete implements AutoCompleteCallback {

	private static final Logger log = LoggerFactory.getLogger(ServiceActionAutoComplete.class);
	private static String ENDPOINT_PREFIX = "--endpoint=";

	@Override
	public void complete(String value, Completion completion, Handler<List<String>> handler)
	{
		// 1. Find if endpoint is set. The argument must be "endpoint".
		// literally.
		String endpointName = findSimpleOptionValue(ENDPOINT_PREFIX, completion);
		if (endpointName != null)
		{
			getServiceActions(endpointName, completion.vertx(), handler);
		} else
		{
			handler.handle(Collections.emptyList());
		}

	}

	private void getServiceActions(String endpoint, Vertx vertx, Handler<List<String>> handler)
	{
		ServiceDiscovery discovery = ServiceDiscovery.create(vertx);
		Function<Record, Boolean> recordFilter = record -> record
				.getLocation()
				.getString("endpoint")
				.equals(endpoint);
		discovery.getRecord(recordFilter, true, ar -> {
			if (ar.failed() || ar.result() == null)
			{
				handler.handle(Collections.emptyList());
			} else
			{
				List<String> result = new ArrayList<>();
				JsonObject actions = ar.result().getMetadata().getJsonObject("actions");
				if (actions != null)
				{
					actions.fieldNames().forEach(result::add);
				}
				handler.handle(result);
			}
			discovery.close();
		});
	}

}
