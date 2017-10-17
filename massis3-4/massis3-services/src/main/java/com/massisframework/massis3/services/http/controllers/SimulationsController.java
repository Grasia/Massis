package com.massisframework.massis3.services.http.controllers;

import static com.massisframework.massis3.services.eventbus.Massis3ServiceUtils.GLOBAL_SERVICE_GROUP;

import com.github.aesteve.vertx.nubes.annotations.Controller;
import com.github.aesteve.vertx.nubes.annotations.auth.Auth;
import com.github.aesteve.vertx.nubes.annotations.params.Param;
import com.github.aesteve.vertx.nubes.annotations.routing.http.GET;
import com.github.aesteve.vertx.nubes.annotations.routing.http.POST;
import com.github.aesteve.vertx.nubes.auth.AuthMethod;
import com.massisframework.massis3.services.eventbus.SimulationServerService;
import com.massisframework.massis3.web.injectors.EventBusSimulationService;
import com.massisframework.massis3.web.response.MassisAPIResponse;

@Controller("/simulation-control")
public class SimulationsController {
	/**
	 * Retrieves the current active simulations
	 */
	@GET("/active")
	public void activeSimulations(
			@EventBusSimulationService(
					literal = true,
					group = GLOBAL_SERVICE_GROUP) SimulationServerService simService,
			MassisAPIResponse response)
	{
		simService.activeSimulations(response.handler());
	}

	/**
	 * Retrieves the current available simulation scenes
	 */
	@GET("/available-scenes")
	public void availableScenes(
			@EventBusSimulationService(
					literal = true,
					group = GLOBAL_SERVICE_GROUP) SimulationServerService simService,
			MassisAPIResponse response)
	{
		simService.availableScenes(response.handler());
	}

	/**
	 * Creates a simulation
	 * 
	 */
	@POST("/create")
	//@Auth(authority = "admin", method = AuthMethod.BASIC)
	public void create(
			@Param(value = "sceneFile", mandatory = true) String sceneFile,
			@EventBusSimulationService(
					literal = true,
					group = GLOBAL_SERVICE_GROUP) SimulationServerService simService,
			MassisAPIResponse response)
	{
		simService.create(sceneFile, response.handler());
	}

	/**
	 * Destroys (stops and undeploy) a running simulation)
	 * 
	 * @param simId
	 *            the simulation id
	 * @param resultHandler
	 */
	@POST("/destroy")
	//@Auth(authority = "admin", method = AuthMethod.BASIC)
	public void destroy(
			@Param(value = "id", mandatory = true) long simId,
			@EventBusSimulationService(
					literal = true,
					group = GLOBAL_SERVICE_GROUP) SimulationServerService simService,
			MassisAPIResponse response)
	{
		simService.destroy(simId, response.handler());
	}
}
