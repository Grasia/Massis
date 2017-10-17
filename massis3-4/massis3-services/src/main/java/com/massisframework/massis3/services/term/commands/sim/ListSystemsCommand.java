package com.massisframework.massis3.services.term.commands.sim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.massisframework.massis3.services.eventbus.Massis3ServiceUtils;
import com.massisframework.massis3.services.eventbus.sim.SystemsService;
import com.massisframework.massis3.services.term.VertxCommandHelper;
import com.massisframework.massis3.services.term.commands.AutoCompletableCommand;
import com.massisframework.massis3.services.term.commands.AutoComplete;

import io.vertx.core.cli.annotations.Description;
import io.vertx.core.cli.annotations.Name;
import io.vertx.core.cli.annotations.Option;
import io.vertx.core.cli.annotations.Summary;
import io.vertx.ext.shell.command.CommandProcess;

@Name("list-systems")
@Summary("Lists the systems running in a simulation")
public class ListSystemsCommand extends AutoCompletableCommand {

	private static final Logger log = LoggerFactory.getLogger(ListSystemsCommand.class);
	private String simulationId;

	@Option(
			shortName = "id",
			argName = "simulationId",
			longName = "simulationId",
			acceptMultipleValues = false,
			flag = false,
			required = true)
	@Description("Simulation id where the operation will be performed")
	@AutoComplete(RunningSimulationsAutoComplete.class)
	public void setSimulationId(String simulationId)
	{
		this.simulationId = simulationId;
	}

	@Override
	public void process(CommandProcess process)
	{
		SystemsService proxy = Massis3ServiceUtils.createProxy(process.vertx(),
				SystemsService.class, this.simulationId);
		proxy.getRunningSystems(ar -> {
			if (ar.failed())
			{
				log.error("Error when retrieving running systems", ar.cause());
				process.write(VertxCommandHelper.errorMsg("Unexpected error", ar.cause()));
			} else
			{
				process.write(VertxCommandHelper.successMsg(ar.result()));
			}
			process.end();
		});
	}

}
