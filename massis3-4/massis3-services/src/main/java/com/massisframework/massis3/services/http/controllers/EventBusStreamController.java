package com.massisframework.massis3.services.http.controllers;

import com.github.aesteve.vertx.nubes.annotations.Controller;
import com.github.aesteve.vertx.nubes.annotations.params.Param;
import com.github.aesteve.vertx.nubes.annotations.routing.http.GET;
import com.massisframework.massis3.services.eventbus.sim.EnvironmentService;
import com.massisframework.massis3.web.injectors.EventBusSimulationService;
import com.massisframework.massis3.web.response.MJPEGStreamer;
import com.massisframework.massis3.web.response.MassisAPIResponse;
import com.massisframework.massis3.web.response.impl.LastEventBusMessageReadStream;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.web.RoutingContext;

@Controller("/stream")
public class EventBusStreamController {

	@GET("/sse/:streamId")
	public void subscribeToSSE(Vertx vertx, @Param("streamId") String streamId)
	{
		vertx.eventBus().consumer(streamId, msg -> {
			// Y handle del event bus
		});
	}

	@GET("/")
	public void cameraVideo(
			RoutingContext rctx,
			@Param("cameraId") String cameraId,
			@EventBusSimulationService(group = "simId") EnvironmentService envService,
			Vertx vertx,
			MassisAPIResponse response)
	{
		envService.cameraMapKeyValue(cameraId, r -> {
			if (r.failed())
			{
				response.handle(r);
			} else
			{

				String key = r.result().getString(0);
				String value = r.result().getString(1);
				// ReadStream<Buffer> readStream = new
				// LocalMapReadStream<>(vertx, 100, key, value);
				ReadStream<Buffer> readStream = LastEventBusMessageReadStream.create(vertx,
						key + value);
				readStream.resume();
				MJPEGStreamer streamer = MJPEGStreamer.create(response.getRoutingContext(),
						readStream);
				streamer.stream();
			}
		});
	}

}
