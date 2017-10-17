package com.massisframework.massis3.services.term.commands;

import static com.massisframework.massis3.services.term.VertxCommandHelper.errorMsg;
import static com.massisframework.massis3.services.term.VertxCommandHelper.recordExtended;
import static com.massisframework.massis3.services.term.VertxCommandHelper.recordSummary;
import static com.massisframework.massis3.services.term.VertxCommandHelper.successMsg;

import java.util.function.Function;

import io.vertx.core.cli.annotations.Argument;
import io.vertx.core.cli.annotations.Description;
import io.vertx.core.cli.annotations.Name;
import io.vertx.core.cli.annotations.Option;
import io.vertx.core.cli.annotations.Summary;
import io.vertx.ext.shell.command.AnnotatedCommand;
import io.vertx.ext.shell.command.CommandProcess;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;

@Name("service-record-info")
@Summary("Shows information about the active services")
public class ListServicesCommand extends AutoCompletableCommand {

	private String endpoint = null;

	@Option(
			shortName = "e",
			argName = "endpoint",
			longName = "endpoint",
			acceptMultipleValues = false,
			flag = false,
			required = false)
	@Description("the endpoint of the service. If provided, an extended info about the service is shown")
	@AutoComplete(ServiceEndpointAutoComplete.class)
	public void setEndpoint(String endpoint)
	{
		this.endpoint = endpoint;
	}

	@Override
	public void process(CommandProcess process)
	{
		if (endpoint == null || this.endpoint.isEmpty())
		{
			listServices(process);
		} else
		{
			showExtendedInfo(process);
		}
	}

	private void showExtendedInfo(CommandProcess process)
	{
		Function<Record, Boolean> recordFilter = record -> record
				.getLocation()
				.getString("endpoint")
				.startsWith(this.endpoint);
		ServiceDiscovery discovery = ServiceDiscovery.create(process.vertx());
		discovery.getRecord(recordFilter, true, ar -> {
			if (ar.failed())
			{
				process.write(errorMsg("error when retrieving record", ar.cause()));
			} else
			{

				Record record = ar.result();
				if (record != null)
				{
					process.write(recordExtended(record));

				} else
				{
					process.write(
							errorMsg("No service found. Is the endpoint correctly typed?"));
				}
			}
			discovery.close();
			process.end();
		});

	}

	private void listServices(CommandProcess process)
	{
	
		ServiceDiscovery discovery = ServiceDiscovery.create(process.vertx());
		discovery.getRecords(r -> true, ar -> {
			if (ar.succeeded())
			{
				process.write(successMsg("Services list retrieved"));
				ar.result().stream().forEach(record -> {
					process.write(recordSummary(record));
				});
				// If the list is not empty, we have matching record
				// Else, the lookup succeeded, but no matching service
			} else
			{
				process.write(
						errorMsg("An error occured when retrieving services list", ar.cause()));
			}
			discovery.close();
			process.end();
		});
	}

}
