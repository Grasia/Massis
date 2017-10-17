package com.massisframework.massis3.web.injectors;

import com.github.aesteve.vertx.nubes.reflections.injectors.typed.ParamInjector;
import com.massisframework.massis3.web.response.MassisAPIResponse;

import io.vertx.ext.web.RoutingContext;

public class MassisAPIResponseInjector implements ParamInjector<MassisAPIResponse> {

	@Override
	public MassisAPIResponse resolve(RoutingContext context)
	{
		return MassisAPIResponse.create(context);
	}

}
