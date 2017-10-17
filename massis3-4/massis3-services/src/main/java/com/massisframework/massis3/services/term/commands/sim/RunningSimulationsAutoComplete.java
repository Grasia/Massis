package com.massisframework.massis3.services.term.commands.sim;

import static com.massisframework.massis3.services.eventbus.Massis3ServiceUtils.GLOBAL_SERVICE_GROUP;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.massisframework.massis3.services.eventbus.Massis3ServiceUtils;
import com.massisframework.massis3.services.eventbus.SimulationServerService;
import com.massisframework.massis3.services.term.commands.AutoComplete.AutoCompleteCallback;

import io.vertx.core.Handler;
import io.vertx.ext.shell.cli.Completion;

public class RunningSimulationsAutoComplete implements AutoCompleteCallback {

	private static final Logger log = LoggerFactory.getLogger(RunningSimulationsAutoComplete.class);

	@Override
	public void complete(String value, Completion completion, Handler<List<String>> handler)
	{
		SimulationServerService proxy = Massis3ServiceUtils.createProxy(completion.vertx(),
				SimulationServerService.class, GLOBAL_SERVICE_GROUP);
		proxy.activeSimulations(ar -> {
			if (ar.failed())
			{
				log.error("Failed to retrieve active simulations", ar.cause());
				handler.handle(Collections.emptyList());
			} else
			{
				List<String> simulations = ar.result()
						.stream()
						.map(String::valueOf)
						.collect(Collectors.toList());

				handler.handle(simulations);
			}
		});

	}

}
