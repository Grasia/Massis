package com.massisframework.massis3.services.term.commands.sim;

import static com.massisframework.massis3.services.eventbus.Massis3ServiceUtils.GLOBAL_SERVICE_GROUP;
import static com.massisframework.massis3.services.eventbus.Massis3ServiceUtils.createProxy;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.massisframework.massis3.services.eventbus.SimulationServerService;
import com.massisframework.massis3.services.term.commands.AutoComplete.AutoCompleteCallback;

import io.vertx.core.Handler;
import io.vertx.core.cli.annotations.Name;
import io.vertx.core.cli.annotations.Summary;
import io.vertx.ext.shell.cli.Completion;


public class SceneFileAutoComplete implements AutoCompleteCallback {

	private static final Logger log = LoggerFactory.getLogger(SceneFileAutoComplete.class);

	@Override
	public void complete(String value, Completion completion, Handler<List<String>> handler)
	{
		// return all scene files directly.
		SimulationServerService proxy = createProxy(
				completion.vertx(),
				SimulationServerService.class,
				GLOBAL_SERVICE_GROUP);

		proxy.availableScenes(ar -> {
			List<String> sceneFiles = new ArrayList<>();
			if (ar.failed())
			{
				log.error("Error when retrieving simulation scene files", ar.cause());
			} else
			{
				ar.result().stream().map(String::valueOf).forEach(sceneFiles::add);
			}
			handler.handle(sceneFiles);
		});
	}

}
