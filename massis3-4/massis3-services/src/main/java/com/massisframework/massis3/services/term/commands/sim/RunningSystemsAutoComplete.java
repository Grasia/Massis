package com.massisframework.massis3.services.term.commands.sim;

import static com.massisframework.massis3.services.term.VertxCommandHelper.findSimpleOptionValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.massisframework.massis3.services.eventbus.Massis3ServiceUtils;
import com.massisframework.massis3.services.eventbus.sim.SystemsService;
import com.massisframework.massis3.services.term.commands.AutoComplete.AutoCompleteCallback;

import io.vertx.core.Handler;
import io.vertx.ext.shell.cli.Completion;

public class RunningSystemsAutoComplete implements AutoCompleteCallback {

	@Override
	public void complete(String value, Completion completion, Handler<List<String>> handler)
	{
		String simId = findSimpleOptionValue("--simulationId", completion);
		if (simId == null)
		{
			handler.handle(Collections.emptyList());
			return;
		}
		SystemsService proxy = Massis3ServiceUtils.createProxy(completion.vertx(),
				SystemsService.class, simId);
		proxy.getRunningSystems(ar -> {
			if (ar.failed())
			{
				ar.cause().printStackTrace();
				handler.handle(Collections.emptyList());
			} else
			{
				// is it using fully qualified name?
				List<String> result = new ArrayList<>();
				if (value.contains("."))
				{
					ar.result().stream().map(String::valueOf).forEach(result::add);
				} else
				{
					ar.result().stream().map(String::valueOf)
							.map(s -> s.substring(s.lastIndexOf('.')+1, s.length()))
							.forEach(result::add);
				}
				handler.handle(result);
			}
		});
	}

}
