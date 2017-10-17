package com.massisframework.massis3.services.term.commands.sim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.massisframework.massis3.services.eventbus.Massis3ServiceUtils;
import com.massisframework.massis3.services.eventbus.sim.SystemsService;
import static com.massisframework.massis3.services.term.VertxCommandHelper.*;
import com.massisframework.massis3.services.term.commands.AutoCompletableCommand;
import com.massisframework.massis3.services.term.commands.AutoComplete;

import io.vertx.core.cli.annotations.Description;
import io.vertx.core.cli.annotations.Name;
import io.vertx.core.cli.annotations.Option;
import io.vertx.core.cli.annotations.Summary;
import io.vertx.ext.shell.command.CommandProcess;

@Name("set-system-status")
@Summary("Enables or disables a simulation system")
public class EnableSystemCommand extends AutoCompletableCommand {

	private static final Logger log = LoggerFactory.getLogger(EnableSystemCommand.class);
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

	private String systemName;

	@Option(
			shortName = "s",
			argName = "system",
			longName = "system",
			acceptMultipleValues = false,
			flag = false,
			required = true)
	@Description("Simulation id where the operation will be performed")
	@AutoComplete(RunningSystemsAutoComplete.class)
	public void setSystemName(String systemName)
	{
		this.systemName = systemName;
	}

	private boolean enabled = false;

	@Option(
			shortName = "e",
			argName = "enabled",
			longName = "enabled",
			acceptMultipleValues = false,
			flag = false,
			required = true,
			choices= {"true","false"}
			)
	@Description("Enables or disables the system")
	public void setSystemEnabled(String enabled)
	{
		this.enabled = Boolean.valueOf(enabled);
	}

	@Override
	public void process(CommandProcess process)
	{
		SystemsService proxy = Massis3ServiceUtils.createProxy(process.vertx(),
				SystemsService.class, this.simulationId);
		if (enabled)
		{
			proxy.enableSystem(this.systemName, ar -> {
				process.write(msg(ar));
				process.end();
			});
		} else
		{
			proxy.disableSystem(this.systemName, ar -> {
				process.write(msg(ar));
				process.end();
			});
		}

	}

}
