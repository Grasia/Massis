package com.massisframework.massis3.services.term.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.massisframework.massis3.services.term.commands.sim.EnableSystemCommand;
import com.massisframework.massis3.services.term.commands.sim.ListSystemsCommand;
import com.massisframework.massis3.services.term.commands.sim.SimulationCreateCommand;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.core.Vertx;
import io.vertx.ext.shell.command.AnnotatedCommand;
import io.vertx.ext.shell.command.Command;
import io.vertx.ext.shell.command.CommandResolver;
import io.vertx.ext.shell.command.base.BusSend;
import io.vertx.ext.shell.command.base.BusTail;
import io.vertx.ext.shell.command.base.Echo;
import io.vertx.ext.shell.command.base.Help;
import io.vertx.ext.shell.command.base.Sleep;
import io.vertx.ext.shell.command.base.VerticleDeploy;
import io.vertx.ext.shell.command.base.VerticleLs;
import io.vertx.ext.shell.command.base.VerticleUndeploy;

public class MassisCommandResolver implements CommandResolver {

	/**
	 * @return the list of base command classes
	 */
	@GenIgnore
	static List<Class<? extends AnnotatedCommand>> baseCommandClasses()
	{
		List<Class<? extends AnnotatedCommand>> list = new ArrayList<>();
		list.add(Echo.class);
		list.add(Sleep.class);
		list.add(Help.class);
		list.add(BusSend.class);
		list.add(BusTail.class);
		list.add(VerticleLs.class);
		list.add(VerticleDeploy.class);
		list.add(VerticleUndeploy.class);

		list.add(ClsCommand.class);
		list.add(ListServicesCommand.class);

		list.add(ServiceCallCommand.class);

		list.add(SimulationCreateCommand.class);
		list.add(ListSystemsCommand.class);
		list.add(EnableSystemCommand.class);

		return list;
	}

	final Vertx vertx;

	public MassisCommandResolver(Vertx vertx)
	{
		this.vertx = vertx;
	}

	@Override
	public List<Command> commands()
	{
		return baseCommandClasses().stream().map(clazz -> Command.create(vertx, clazz))
				.collect(Collectors.toList());
	}
}
