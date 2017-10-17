package com.massisframework.massis3.services.term.commands;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.massisframework.massis3.services.term.commands.AutoComplete.AutoCompleteCallback;

import io.vertx.core.Handler;
import io.vertx.ext.shell.cli.Completion;
import io.vertx.servicediscovery.ServiceDiscovery;

public class ServiceEndpointAutoComplete implements AutoCompleteCallback {

	@Override
	public void complete(final String val, Completion completion, Handler<List<String>> handler)
	{
		ServiceDiscovery discovery = ServiceDiscovery.create(completion.vertx());
		discovery.getRecords(r -> r.getLocation().getString("endpoint").startsWith(val), ar -> {
			if (ar.failed())
			{
				handler.handle(Collections.emptyList());
			} else
			{
				List<String> endpoints = ar.result().stream()
						.map(r -> r.getLocation().getString("endpoint"))
						.collect(Collectors.toList());
				handler.handle(endpoints);
			}
			discovery.close();
		});
	}

}
