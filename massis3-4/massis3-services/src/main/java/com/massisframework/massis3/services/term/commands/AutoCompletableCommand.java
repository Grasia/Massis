package com.massisframework.massis3.services.term.commands;

import static com.massisframework.massis3.services.term.VertxCommandHelper.completeWithPrefix;
import static com.massisframework.massis3.services.term.VertxCommandHelper.getAutoCompleteCallback;
import static com.massisframework.massis3.services.term.VertxCommandHelper.lastTextToken;
import static com.massisframework.massis3.services.term.VertxCommandHelper.optionCompletions;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.massisframework.massis3.services.term.commands.AutoComplete.AutoCompleteCallback;

import io.vertx.ext.shell.cli.Completion;
import io.vertx.ext.shell.command.AnnotatedCommand;

public abstract class AutoCompletableCommand extends AnnotatedCommand {

	private static final Logger log = LoggerFactory.getLogger(AutoCompletableCommand.class);

	@Override
	public void complete(Completion completion)
	{
		List<String> optionCompletions = optionCompletions(getClass(), completion);
		String lastToken = lastTextToken(completion);
		if (optionCompletions.size() > 0)
		{
			if (optionCompletions.size() == 1)
			{

				String completionVal = optionCompletions.get(0);
				completion.complete(completionVal.substring(lastToken.length()), false);
			} else
			{
				completeWithPrefix(lastToken, optionCompletions, completion);
			}
		} else
		{
			if (lastToken == null)
			{
				completion.complete(Collections.emptyList());
				return;
			}
			try
			{
				int equalIndex = lastToken.indexOf("=");
				if (!lastToken.startsWith("--") || equalIndex <= 0)
				{
					completion.complete(Collections.emptyList());
					return;
				}
				String providedName = lastToken.substring(2, equalIndex);
				String optionValue = lastToken.substring(equalIndex + 1);
				AutoCompleteCallback cb = getAutoCompleteCallback(getClass(), providedName);
				cb.complete(optionValue, completion, (options) -> {

					completeWithPrefix(optionValue, options, completion);
				});
			} catch (Exception e)
			{
				log.error("Error while invoking autocomplete", e);
				completion.complete(Collections.emptyList());
			}
		}
	}

}
