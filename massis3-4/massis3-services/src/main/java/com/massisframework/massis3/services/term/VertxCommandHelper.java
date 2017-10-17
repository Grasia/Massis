package com.massisframework.massis3.services.term;

import static org.fusesource.jansi.Ansi.ansi;
import static org.fusesource.jansi.Ansi.Color.CYAN;
import static org.fusesource.jansi.Ansi.Color.GREEN;
import static org.fusesource.jansi.Ansi.Color.RED;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.massisframework.massis3.services.Responses;
import com.massisframework.massis3.services.term.commands.AutoComplete;
import com.massisframework.massis3.services.term.commands.AutoComplete.AutoCompleteCallback;

import io.vertx.core.AsyncResult;
import io.vertx.core.cli.annotations.Option;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.shell.cli.CliToken;
import io.vertx.ext.shell.cli.Completion;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.Status;

public class VertxCommandHelper {

	private static final Logger log = LoggerFactory.getLogger(VertxCommandHelper.class);

	public static String welcomeText()
	{
		try (InputStream is = VertxCommandHelper.class
				.getResourceAsStream("welcome-message.txt"))
		{
			return ansi().fg(CYAN).a(IOUtils.toString(is, "UTF-8")).reset().toString();

		} catch (IOException e)
		{
			log.error("Error when loading welcome message", e);
			return "MASSIS Terminal";
		}
	}

	public static <T> String msg(AsyncResult<T> ar)
	{
		if (ar.failed())
		{
			return errorMsg("Error when performing operation", ar.cause());
		} else
		{
			return successMsg(ar.result());
		}
	}

	public static String errorMsg(String msg, Throwable e)
	{

		String errorMessage = Responses.jsonResponseError(500, msg, e)
				.encodePrettily()
				.replace("\\n", "\n")
				.replace("\\t", "\t");
		Ansi ansi = ansi().newline()
				.bold().fg(RED).a("[ERROR]: ").boldOff()
				.fgBrightRed()
				.a(msg)
				.newline()
				.a(errorMessage)
				.newline();

		return ansi.reset().toString();
	}

	public static String errorMsg(String str)
	{
		return errorMsg(str, null);
	}

	public static String successMsg(Object obj)
	{
		return ansi().newline().bold().fg(GREEN).a("[SUCCESS]: Command succeeded").boldOff()
				.fgBrightGreen()
				.newline()
				.a(Responses.jsonResponseOK(obj).encodePrettily())
				.newline()
				.reset().toString();
	}

	public static String recordSummary(Record r)
	{

		return recordSummaryAnsi(r).reset().toString();

	}

	public static String findSimpleOptionValue(String prefix, Completion completion)
	{
		return completion.lineTokens()
				.stream()
				.filter(ct -> ct.isText())
				.map(ct -> ct.value())
				.filter(text -> text.startsWith(prefix))
				.findAny()
				.map(s -> s.substring(prefix.length()+1)).orElse(null);
	}

	private static Ansi recordSummaryAnsi(Record r)
	{
		JsonObject metadata = r.getMetadata();
		String interfaceName = String.valueOf(metadata.getString("service.interface"));
		String endpoint = String.valueOf(r.getLocation().getString("endpoint"));
		return ansi()
				.newline()
				.a(Attribute.UNDERLINE).a("Record: ").a(Attribute.UNDERLINE_OFF)
				.a(r.getName()).newline()
				.a(Attribute.UNDERLINE).a("\tJava Interface: ").a(Attribute.UNDERLINE_OFF)
				.a(interfaceName).newline()
				.a(Attribute.UNDERLINE).a("\tEndpoint:       ").a(Attribute.UNDERLINE_OFF)
				.a(endpoint).newline()
				.a(Attribute.UNDERLINE).a("\tStatus:         ")
				.a(Attribute.UNDERLINE_OFF)
				.fg(r.getStatus() == Status.UP ? GREEN : RED).a(r.getStatus()).fgDefault()
				.newline();
	}

	public static String recordExtended(Record r)
	{
		JsonObject metadata = r.getMetadata();
		Ansi ansi = recordSummaryAnsi(r).reset();

		ansi.a(Attribute.UNDERLINE).a("\tActions:").a(Attribute.UNDERLINE_OFF).newline();
		JsonObject actions = metadata.getJsonObject("actions");
		if (actions != null)
		{
			actions.fieldNames().forEach(name -> {
				JsonObject action = actions.getJsonObject(name);
				ansi.a(Attribute.UNDERLINE).a("\t\t").a(name).a(Attribute.UNDERLINE_OFF)
						.a("(")
						.a(action.getJsonObject("parameters").getJsonObject("properties")
								.fieldNames())
						.a(")")
						.newline();
				;
			});
		}
		return ansi.reset().toString();
	}

	public static List<Option> getOptionsOf(Class<?> clazz)
	{
		return Arrays.stream(clazz.getMethods())
				.filter(m -> m.isAnnotationPresent(Option.class))
				.map(m -> m.getAnnotation(Option.class))
				.collect(Collectors.toList());
	}

	public static String lastTextToken(Completion completion)
	{
		List<CliToken> tokens = completion.lineTokens();
		for (int i = tokens.size() - 1; i >= 0; i--)
		{
			CliToken token = tokens.get(i);
			if (token.isText())
			{
				return token.value();
			}
		}
		return null;

	}

	public static List<String> optionCompletions(Class<?> cmdType, Completion completion)
	{
		List<Option> options = getOptionsOf(cmdType);
		String value = lastTextToken(completion);
		if (value != null && value.startsWith("--") && !value.contains("="))
		{
			String providedName = value.substring(2);
			return options.stream()
					.filter(option -> option.longName().startsWith(providedName))
					.map(option -> "--" + option.longName())
					.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	public static AutoCompleteCallback getAutoCompleteCallback(Class<?> cmdType, String longName)
	{
		for (Method method : cmdType.getMethods())
		{
			Option option = method.getAnnotation(Option.class);
			if (option != null && option.longName().equals(longName))
			{
				AutoComplete ac = method.getAnnotation(AutoComplete.class);
				if (ac != null)
				{
					try
					{
						Constructor<? extends AutoCompleteCallback> ctor = ac.value()
								.getDeclaredConstructor();
						ctor.setAccessible(true);
						AutoCompleteCallback callback = ctor.newInstance();
						return callback;
					} catch (Exception e)
					{
						throw new RuntimeException(e);
					}

				}
			}
		}
		return (s, session, completion) -> {
			System.out.println("Argument has no autocompletion");
			completion.handle(Collections.emptyList());
		};
	}

	public static void completeWithPrefix(String prefix, List<String> options,
			Completion completion)
	{

		options = options.stream().filter(o -> o.startsWith(prefix)).collect(Collectors.toList());

		String commonPrefix = Completion.findLongestCommonPrefix(options);
		if (commonPrefix.length() > prefix.length())
		{
			completion.complete(commonPrefix.substring(prefix.length()), false);
		} else
		{
			completion.complete(options);
		}
	}

}
