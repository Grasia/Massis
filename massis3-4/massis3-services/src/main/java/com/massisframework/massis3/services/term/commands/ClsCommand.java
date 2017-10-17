package com.massisframework.massis3.services.term.commands;

import static org.fusesource.jansi.Ansi.ansi;

import com.massisframework.massis3.services.term.VertxCommandHelper;

import io.vertx.core.cli.annotations.Name;
import io.vertx.core.cli.annotations.Summary;
import io.vertx.ext.shell.command.CommandProcess;

@Name("clear")
@Summary("Clears the screen")
public class ClsCommand extends AutoCompletableCommand {

	@Override
	public void process(CommandProcess process)
	{
		process.write(ansi().eraseScreen().a(VertxCommandHelper.welcomeText()).reset().toString())
				.end();
	}

}