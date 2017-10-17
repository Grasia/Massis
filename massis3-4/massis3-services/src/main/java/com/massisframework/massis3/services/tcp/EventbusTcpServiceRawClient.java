package com.massisframework.massis3.services.tcp;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import io.vertx.ext.eventbus.bridge.tcp.impl.protocol.FrameHelper;
import io.vertx.ext.eventbus.bridge.tcp.impl.protocol.FrameParser;

public class EventbusTcpServiceRawClient {

	private static final Logger log = LoggerFactory.getLogger(EventbusTcpServiceRawClient.class);
	private String host;
	private int port;
	private Vertx vertx;
	@SuppressWarnings("rawtypes")
	private Map<String, Handler<AsyncResult>> replyHandlers;
	private NetSocket socket;

	public EventbusTcpServiceRawClient(Vertx vertx, String host, int port)
	{
		this.host = host;
		this.port = port;
		this.vertx = vertx;
		this.replyHandlers = new ConcurrentHashMap<>();
	}

	public void init(Handler<AsyncResult<Void>> handler)
	{
		createClient(handler);
	}

	private void createClient(Handler<AsyncResult<Void>> handler)
	{
		vertx.createNetClient().connect(this.port, this.host, r -> {
			if (r.failed())
			{
				log.error("error", r.cause());
				handler.handle(Future.failedFuture(r.cause()));
			} else
			{
				NetSocket socket = r.result();
				final FrameParser parser = new FrameParser(rparse -> {
					if (r.failed())
					{
						log.error("Error when receiving frame", rparse.cause());
					} else
					{
						onData(rparse.result());
					}
				});
				this.socket = socket;
				socket.handler(parser);
				handler.handle(Future.succeededFuture());
			}
		});
	}

	public <T> void send(
			String addr,
			JsonObject body,
			DeliveryOptions deliveryOptions,
			Handler<AsyncResult<T>> handler)
	{

		String replyAddr = newUUID();
		this.replyHandlers.put(replyAddr, (Handler) handler);
		FrameHelper.sendFrame("send", addr, replyAddr, getJsonHeaders(deliveryOptions), true, body,
				socket);
	}

	private JsonObject getJsonHeaders(DeliveryOptions deliveryOptions)
	{
		JsonObject obj = new JsonObject();
		deliveryOptions.getHeaders().entries().stream()
				.forEach(e -> obj.put(e.getKey(), e.getValue()));
		return obj;
	}

	private void onData(JsonObject frame)
	{

		if (log.isInfoEnabled())
		{
			log.info("Received data from server: {0}", frame.toString());
		}
		String destAddress = frame.getString("address");
		Handler<AsyncResult> replyHandler = this.replyHandlers.remove(destAddress);
		if (replyHandler != null)
		{
			switch (frame.getString("type"))
			{
			case "err":
				// int failureCode = frame.getInteger("failureCode");
				// msg.fail(failureCode, jsonRepl.getString("message"));
				log.error("Failure when receiving message. {0}", frame.toString());
				replyHandler.handle(Future.failedFuture(frame.getJsonObject("body").encode()));
				break;
			case "message":
				replyHandler.handle(Future.succeededFuture(frame.getValue("body")));
				break;
			}
		} else
		{
			log.error("No reply handler found. Message: {0}", frame.encode());
		}
	}

	private static String newUUID()
	{
		return UUID.randomUUID().toString();
	}

}
