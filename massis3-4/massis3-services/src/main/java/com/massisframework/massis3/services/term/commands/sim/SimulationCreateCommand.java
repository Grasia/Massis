package com.massisframework.massis3.services.term.commands.sim;

import static com.massisframework.massis3.services.eventbus.Massis3ServiceUtils.GLOBAL_SERVICE_GROUP;
import static com.massisframework.massis3.services.eventbus.Massis3ServiceUtils.createProxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.massisframework.massis3.services.eventbus.SimulationServerService;
import com.massisframework.massis3.services.term.VertxCommandHelper;
import com.massisframework.massis3.services.term.commands.AutoCompletableCommand;
import com.massisframework.massis3.services.term.commands.AutoComplete;

import io.vertx.core.cli.annotations.Description;
import io.vertx.core.cli.annotations.Name;
import io.vertx.core.cli.annotations.Option;
import io.vertx.core.cli.annotations.Summary;
import io.vertx.ext.shell.command.CommandProcess;

@Name("create-simulation")
@Summary("Creates an empty simulation")
public class SimulationCreateCommand extends AutoCompletableCommand {

	private static final Logger log = LoggerFactory.getLogger(SimulationCreateCommand.class);
	private String sceneFile;

	@Option(
			shortName = "s",
			argName = "sceneFile",
			longName = "sceneFile",
			acceptMultipleValues = false,
			flag = false,
			required = false)
	@Description("Base scene file of the simulation")
	@AutoComplete(SceneFileAutoComplete.class)
	public void setSceneFile(String sceneFile)
	{
		this.sceneFile = sceneFile;
	}

	@Override
	public void process(CommandProcess process)
	{
		SimulationServerService proxy = createProxy(
				process.vertx(),
				SimulationServerService.class,
				GLOBAL_SERVICE_GROUP);
		proxy.create(sceneFile, ar -> {
			if (ar.failed())
			{
				log.error("Error when calling simulation service", ar.cause());
			} else
			{
				process.write(VertxCommandHelper.successMsg((ar.result())));
			}
			process.end();
		});
	}

}
