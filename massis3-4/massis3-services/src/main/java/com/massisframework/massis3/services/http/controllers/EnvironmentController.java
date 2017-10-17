package com.massisframework.massis3.services.http.controllers;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.aesteve.vertx.nubes.annotations.Controller;
import com.github.aesteve.vertx.nubes.annotations.View;
import com.github.aesteve.vertx.nubes.annotations.mixins.ContentType;
import com.github.aesteve.vertx.nubes.annotations.params.ContextData;
import com.github.aesteve.vertx.nubes.annotations.params.Param;
import com.github.aesteve.vertx.nubes.annotations.params.RequestBody;
import com.github.aesteve.vertx.nubes.annotations.routing.http.GET;
import com.github.aesteve.vertx.nubes.annotations.routing.http.POST;
import com.massisframework.massis3.services.dataobjects.JsonPoint;
import com.massisframework.massis3.services.eventbus.sim.EnvironmentService;
import com.massisframework.massis3.web.injectors.EventBusSimulationService;
import com.massisframework.massis3.web.response.MJPEGStreamer;
import com.massisframework.massis3.web.response.MassisAPIResponse;
import com.massisframework.massis3.web.response.impl.LastEventBusMessageReadStream;
import com.massisframework.massis3.web.response.impl.LocalMapReadStream;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.web.RoutingContext;

@Controller("/simulations/:simId/environment")
@ContentType({ "application/json", "*/*" })
public class EnvironmentController {

	private static final Logger log = LoggerFactory.getLogger(EnvironmentController.class);

	/**
	 * Retrieves the id of the areas (rooms) of the simulation environment
	 * 
	 */
	@GET("/roomIds")
	public void roomIds(
			@EventBusSimulationService(group = "simId") EnvironmentService envService,
			MassisAPIResponse response)
	{
		envService.roomIds(response.handler());
	}

	@GET("/cameraIds")
	public void cameraIds(
			@EventBusSimulationService(group = "simId") EnvironmentService envService,
			MassisAPIResponse response)
	{
		envService.cameraIds(response.handler());
	}

	@GET("/rooms/")
	public void allRoomsInfo(
			@EventBusSimulationService(group = "simId") EnvironmentService envService,
			MassisAPIResponse response)
	{
		envService.allRoomsInfo(response.handler());
	}

	/**
	 * 
	 * @param id
	 *            the id og f the room
	 * @param resultHandler
	 *            the information of the room, following the schema:
	 * 
	 */
	@GET("/rooms/:roomId")
	public void roomInfo(@Param("roomId") long id,
			@EventBusSimulationService(group = "simId") EnvironmentService envService,
			MassisAPIResponse response)
	{
		envService.roomInfo(id, response.handler());
	}

	/**
	 * 
	 * @param name
	 *            the name of the entity to be searched
	 * @param resultHandler
	 *            A json array of the simulation entities that have the name
	 *            provided. The entities are returned as their id.
	 */
	@GET("/entitiesNamed")
	public void entitiesNamed(@Param("name") String name,
			@EventBusSimulationService(group = "simId") EnvironmentService envService,
			MassisAPIResponse response)
	{
		envService.entitiesNamed(name, response.handler());
	}

	/**
	 * 
	 * @param id
	 *            the id of the entity
	 * @param resultHandler
	 *            the location of the entity
	 */
	@GET("/entity/:entityId")
	public void entityLocation(
			@Param("entityId") long id,
			@EventBusSimulationService(group = "simId") EnvironmentService envService,
			MassisAPIResponse response)
	{
		envService.entityLocation(id, response.handler());
	}

	@GET("/time")
	public void time(
			@EventBusSimulationService(group = "simId") EnvironmentService envService,
			MassisAPIResponse response)
	{
		envService.timeStreamAddress(r -> {
			if (r.failed())
			{
				response.handle(r);
				return;
			} else
			{
				response.getRoutingContext().reroute("/stream/sse" + r.result());
			}
		});
	}

	@GET("/sceneName")
	public void sceneName(
			@EventBusSimulationService(group = "simId") EnvironmentService envService,
			MassisAPIResponse response)
	{
		envService.sceneName(response.handler());
	}

	@POST("/addCamera")
	public void addCamera(
			@EventBusSimulationService(group = "simId") EnvironmentService envService,
			MassisAPIResponse response)
	{
		envService.addCamera(response.handler());
	}

	@POST("/camera/:cameraId/setLocation")
	@ContentType("application/json")
	public void setCameraLocation(
			@Param("cameraId") String cameraId,
			@RequestBody JsonPoint location,
			@EventBusSimulationService(group = "simId") EnvironmentService envService,
			MassisAPIResponse response)
	{
		System.out.println("UH: " + location);
		response.getRoutingContext().response().end("asd");
		// envService.setCameraLocation(cameraId, location, response.handler());
	}

	@GET("/camera/:cameraId/video")
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
				// String address = r.result();
				String localMapName = r.result().getString(0);
				String localMapKey = r.result().getString(1);
				ReadStream<Buffer> readStream = new LocalMapReadStream<String, Buffer>(vertx, 100L,
						localMapName, localMapKey);
				readStream.resume();
				MJPEGStreamer streamer = MJPEGStreamer.create(response.getRoutingContext(),
						readStream);
				streamer.stream();
			}
		});

	}

	@GET("/camera/:cameraId/static")
	public void cameraStatic(
			RoutingContext rctx,
			@Param("cameraId") String cameraId,
			@EventBusSimulationService(group = "simId") EnvironmentService envService,
			Vertx vertx,
			MassisAPIResponse response)
	{
		rctx.response().setChunked(true);
		rctx.response().putHeader("Content-Type", "image/jpeg");
		envService.cameraMapKeyValue(cameraId, r -> {
			if (r.failed())
			{
				response.handle(r);
			} else
			{

				String key = r.result().getString(0);
				String value = r.result().getString(1);
				Buffer buff = (Buffer) vertx.sharedData().getLocalMap(key).get(value);
				if (buff != null)
				{
					rctx.response().putHeader("Content-Length", "" + buff.length());
					rctx.response().end(buff);
				} else
				{
					rctx.response().end();
				}

			}
		});
	}

	@GET("/cameraweb")
	@View("camera-web.hbs")
	public void cameraWeb(
			Vertx vertx,
			@ContextData Map<String, Object> data,
			@Param(value = "simId", mandatory = true) String simId)
	{
		data.put("refreshTime", 100);
		data.put("simId", simId);
	}

	@GET("/ebExample")
	@View("ebexample.hbs")
	public void ebexample(
			Vertx vertx,
			@ContextData Map<String, Object> data,
			@Param("simId") String simId,
			@Param("cameraId") String cameraId)
	{

	}

}
