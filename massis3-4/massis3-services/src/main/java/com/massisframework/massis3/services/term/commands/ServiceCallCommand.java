package com.massisframework.massis3.services.term.commands;

import static com.massisframework.massis3.services.term.VertxCommandHelper.errorMsg;
import static com.massisframework.massis3.services.term.VertxCommandHelper.successMsg;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.massisframework.massis3.services.term.commands.AutoComplete.AutoCompleteCallback;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.cli.annotations.Argument;
import io.vertx.core.cli.annotations.DefaultValue;
import io.vertx.core.cli.annotations.Description;
import io.vertx.core.cli.annotations.Name;
import io.vertx.core.cli.annotations.Option;
import io.vertx.core.cli.annotations.Summary;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.shell.command.CommandProcess;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;

@Name("service-call")
@Summary("Executes a direct call to a service. "
		+ "The last parameter should be a valid JSON object, single quoted.")
public class ServiceCallCommand extends AutoCompletableCommand {

	private static final Logger log = LoggerFactory.getLogger(ServiceCallCommand.class);

	private String endpoint = null;
	private String action = null;
	private String params = null;

	@Option(
			required = true,
			argName = "Endpoint of the service",
			longName = "endpoint",
			shortName = "e")
	@Description("the endpoint of the service to be called.")
	@AutoComplete(ServiceEndpointAutoComplete.class)
	public void setEndpoint(String endpoint)
	{
		this.endpoint = endpoint;
	}

	@Option(
			required = true,
			argName = "Action to be executed",
			longName = "action",
			shortName = "a")
	@Description("Action (method) name of the service to be executed")
	@AutoComplete(ServiceActionAutoComplete.class)
	public void setAction(String act)
	{
		this.action = act;
	}

	@Argument(
			argName = "parameters",
			required = false,
			index = 2)
	@DefaultValue("{}")
	@Description("Parameters of the service in json format")
	public void setParameters(String params)
	{
		this.params = params;
	}

	// TODO duplicated code with http
	@Override
	public void process(CommandProcess process)
	{
		try
		{
			JsonObject _json = new JsonObject(this.params);

			ServiceDiscovery discovery = ServiceDiscovery.create(process.vertx());

			Function<Record, Boolean> recordFilter = record -> record
					.getLocation()
					.getString("endpoint")
					.equals(endpoint);
			discovery.getRecord(recordFilter, true, ar -> {
				if (ar.failed())
				{
					process.write(errorMsg("Error when retrieving records", ar.cause()));
				} else
				{
					Record record = ar.result();
					if (record == null)
					{
						process.write(
								errorMsg("No service found. Is the endpoint correctly typed?"));
						discovery.close();
						process.end();
						return;
					}

					JsonObject actions = record.getMetadata().getJsonObject("actions");

					if (actions == null)
					{
						process.write(errorMsg("This service can't be called from http"));
						discovery.close();
						process.end();
						return;

					}
					if (!actions.containsKey(action))
					{
						process.write(errorMsg("Action " + action
								+ " is not offered by the service. Offered Actions: "
								+ actions.fieldNames()));
						discovery.close();
						process.end();
						return;

					}

					Vertx vertx = process.vertx();
					DeliveryOptions _deliveryOptions = new DeliveryOptions();
					_deliveryOptions.addHeader("action", action);
					vertx.eventBus().send(endpoint, _json, _deliveryOptions, ar2 -> {
						if (ar2.succeeded())
						{
							process.write(successMsg(ar2.result().body()));
						} else
						{
							process.write(errorMsg("Error when calling service", ar2.cause()));
						}

						discovery.close();
						process.end();
						return;
					});

				}
			});

		} catch (Exception e)
		{
			log.error("error when parsing json in command", e);
			process.write(errorMsg("Error when parsing JSON", e));
			process.end();
			return;
		}
	}

}
