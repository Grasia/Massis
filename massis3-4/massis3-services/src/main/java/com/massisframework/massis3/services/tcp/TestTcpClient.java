package com.massisframework.massis3.services.tcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.massisframework.massis3.services.eventbus.EchoService;
import com.massisframework.massis3.services.eventbus.impl.EchoServiceImpl;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.ext.bridge.BridgeOptions;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.eventbus.bridge.tcp.TcpEventBusBridge;
import io.vertx.ext.eventbus.bridge.tcp.impl.protocol.FrameHelper;
import io.vertx.ext.eventbus.bridge.tcp.impl.protocol.FrameParser;
import io.vertx.serviceproxy.ProxyHelper;

public class TestTcpClient {

	private static final Logger log = LoggerFactory.getLogger(TestTcpClient.class);
	static int bridgePort = 7000;
	static String host = "localhost";
	static Vertx vertx = Vertx.vertx();

	public static void main(String[] args)
	{

		ProxyHelper.registerService(EchoService.class, vertx, new EchoServiceImpl(vertx), "echo");
		BridgeOptions bridgeOptions = new BridgeOptions()
				.addInboundPermitted(new PermittedOptions().setAddress("echo"))
				.addOutboundPermitted(new PermittedOptions());
		NetServerOptions netOptions = new NetServerOptions().setPort(bridgePort);
		TcpEventBusBridge bridge = new MassisTcpEventBusBridgeImpl(vertx, bridgeOptions, netOptions);
		
		bridge.listen(res -> {
			log.info("TCP server running");
			launchClient1();
		});

	}

	private static void launchClient1()
	{
		EventbusTcpServiceRawClient rawClient = new EventbusTcpServiceRawClient(vertx, host,
				bridgePort);
		rawClient.init(r -> {
			if (r.failed())
			{
				log.error("Error when initiating client connection", r.cause());
			} else
			{
				JsonObject _json = new JsonObject();
				_json.put("message", 8);
				DeliveryOptions _deliveryOptions = new DeliveryOptions();
				_deliveryOptions.addHeader("action", "echoInteger");
				rawClient.send("echo", _json, _deliveryOptions, res -> {
					if (res.failed())
					{
						log.error("FAIL", res.cause());
						// handler.handle(Future.failedFuture(res.cause()));
					} else
					{
						System.out.println(res.result() + " YEAG");
						// handler.handle(Future.succeededFuture(res.result().body()));
					}
				});

				// "echo", new , deliveryOptions, handler);
			}
		});
	}

}
