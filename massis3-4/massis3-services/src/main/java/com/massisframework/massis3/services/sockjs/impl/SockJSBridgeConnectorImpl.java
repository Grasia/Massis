package com.massisframework.massis3.services.sockjs.impl;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.MessageProducer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebSocketFrame;
import io.vertx.core.json.JsonObject;

public class SockJSBridgeConnectorImpl {

	private static final Logger log = LoggerFactory.getLogger(SockJSBridgeConnectorImpl.class);

	private Vertx vertx;
	private String websocketPath;
	private HttpClient httpClient;
	private HttpClientOptions httpOptions;
	private WebSocket websocket;

	private Map<String, Handler<AsyncResult<JsonObject>>> clientHandlers;

	public SockJSBridgeConnectorImpl(Vertx vertx, String websocketPath, HttpClientOptions options)
	{
		this.vertx = vertx;
		this.websocketPath = websocketPath;
		this.httpOptions = new HttpClientOptions(options);
		this.clientHandlers = new ConcurrentHashMap<>();
	}

	public void connect()
	{
		this.httpClient = vertx.createHttpClient(httpOptions);
	}

	private void getWebSocket(Handler<AsyncResult<WebSocket>> handler)
	{
		synchronized (this)
		{
			if (this.websocket != null)
			{
				handler.handle(Future.succeededFuture(this.websocket));
				return;
			}

			if (this.websocket == null)
			{
				httpClient.websocket(websocketPath, ws -> {
					try
					{
						this.websocket = ws;
						this.websocket.handler(this::onServerMessage);
						this.websocket.exceptionHandler(this::onWebSocketException);
						this.websocket.closeHandler(this::onWebSocketClose);
						startKeepAliveLoop();
						handler.handle(Future.succeededFuture(this.websocket));
					} catch (Exception e)
					{
						handler.handle(Future.failedFuture(e));
					}
				});
			}
		}
	}

	private void onWebSocketClose(Void _void)
	{
		synchronized (this)
		{
			this.websocket = null;
		}
	}

	private void onWebSocketException(Throwable ex)
	{
		log.error("Exception caught in websocket", ex);
	}

	private void sendToServer(
			String serverAddress,
			Object body,
			boolean isSend,
			MultiMap headers,
			Handler<AsyncResult<JsonObject>> replyHandler)
	{
		JsonObject payload = new JsonObject();
		SockJsProxyFrameHelper.setSendType(isSend, payload);
		SockJsProxyFrameHelper.setHeaders(headers, payload);
		SockJsProxyFrameHelper.setBody(body, payload);
		SockJsProxyFrameHelper.setAddress(serverAddress, payload);

		if (replyHandler != null)
		{
			String replyAddress = newBridgeUUID();
			SockJsProxyFrameHelper.setReplyAddress(replyAddress, payload);
			this.clientHandlers.put(replyAddress, r -> {
				this.clientHandlers.remove(replyAddress);
				replyHandler.handle(r);
			});
		}

		rawWrite(payload, ex -> {
			if (ex != null)
			{
				String replyAddress = SockJsProxyFrameHelper.getReplyAddress(payload);
				if (replyAddress != null)
				{
					this.clientHandlers.remove(replyAddress);
					if (replyHandler != null)
					{
						replyHandler.handle(Future.failedFuture(ex));
					}
				}
			}
		});

	}

	private <T> MessageProducer<T> sender(boolean isSend, String serverAddr)
	{
		EventBus eb = vertx.eventBus();
		String aliasedAddr = newBridgeUUID();
		CloseableMessageProducer<T> sender = new CloseableMessageProducer<>(eb.sender(aliasedAddr));
		// register.
		MessageConsumer<T> consumer = eb.<T> consumer(aliasedAddr, msg -> {
			Handler<AsyncResult<JsonObject>> clientReplyHandler = clientReplyHandler(msg);
			this.sendToServer(serverAddr, msg.body(), isSend, msg.headers(), clientReplyHandler);
		});
		sender.closeHandler(r -> {
			consumer.unregister();
		});
		return sender;
	}

	private <T> Handler<AsyncResult<JsonObject>> clientReplyHandler(Message<T> msg)
	{
		if (msg.replyAddress() == null)
			return null;

		return serverR -> {
			JsonObject payload = serverR.result();
			if (serverR.failed())
			{
				msg.fail(-1, serverR.cause().getMessage());
				return;
			}
			if (SockJsProxyFrameHelper.isError(payload))
			{
				msg.fail(-1, SockJsProxyFrameHelper.getMessage(payload));
				return;
			}

			Object body = SockJsProxyFrameHelper.getBody(payload);

			if (!SockJsProxyFrameHelper.hasReplyAddress(payload))
			{
				msg.reply(body);
				return;
			}

			msg.reply(body, r -> {

				String serverAddr = SockJsProxyFrameHelper.getReplyAddress(payload);

				if (r.failed())
				{
					// TODO How to send a failed message to the server?
					log.error("Failed message", r.cause());
				}

				this.sendToServer(
						serverAddr,
						r.result().body(),
						true,
						r.result().headers(),
						clientReplyHandler(r.result()));
			});

		};
	}

	private void onServerMessage(Buffer buffer)
	{
		JsonObject payload = new JsonObject(buffer);
		String address = SockJsProxyFrameHelper.getAddress(payload);
		Handler<AsyncResult<JsonObject>> handler = this.clientHandlers.get(address);
		// direccion del server X.
		if (SockJsProxyFrameHelper.isError(payload))
		{
			handler.handle(Future.failedFuture(SockJsProxyFrameHelper.getMessage(payload)));
		} else
		{
			handler.handle(Future.succeededFuture(payload));
		}
	}

	// String type = SockJsProxyFrameHelper.getType(payload);
	// MultiMap headers = SockJsProxyFrameHelper.getHeaders(payload);
	// Object body = SockJsProxyFrameHelper.getBody(payload);
	//
	// String replyAddress = SockJsProxyFrameHelper.getReplyAddress(payload);

	private void rawWrite(JsonObject msg, Handler<Throwable> handler)
	{

		if (log.isInfoEnabled())
		{
			log.info("Writing data to server: " + msg.encode());
		}
		getWebSocket(r -> {
			if (r.failed())
			{
				log.error("Error when retrieving websocket connection");
			} else
			{
				WebSocket ws = r.result();
				try
				{
					ws.writeFrame(WebSocketFrame.textFrame(msg.encode(), true));
					handler.handle(null);
				} catch (Exception e)
				{
					handler.handle(e);
				}
			}
		});

	}

	private String newBridgeUUID()
	{
		return UUID.randomUUID().toString();
	}

	private void startKeepAliveLoop()
	{

	}

}
