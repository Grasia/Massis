package com.massisframework.massis3.web.controllers;

import java.util.Map;

import com.github.aesteve.vertx.nubes.annotations.Controller;
import com.github.aesteve.vertx.nubes.annotations.View;
import com.github.aesteve.vertx.nubes.annotations.params.ContextData;
import com.github.aesteve.vertx.nubes.annotations.routing.http.GET;

@Controller("/dashboard")
public class Dashboard {

	@GET("/")
	@View("dashboard.jade")
	public void handle(@ContextData Map<String, Object> context)
	{
		context.put("runningSimulations", 8);
	}

}
