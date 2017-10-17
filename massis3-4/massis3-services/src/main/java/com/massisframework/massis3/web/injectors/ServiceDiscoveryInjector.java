package com.massisframework.massis3.web.injectors;

import com.github.aesteve.vertx.nubes.reflections.injectors.typed.ParamInjector;

import io.vertx.ext.web.RoutingContext;
import io.vertx.servicediscovery.ServiceDiscovery;

public class ServiceDiscoveryInjector implements ParamInjector<ServiceDiscovery> {

	private ServiceDiscovery sc;

	public ServiceDiscoveryInjector(ServiceDiscovery sc)
	{
		this.sc = sc;
	}

	@Override
	public ServiceDiscovery resolve(RoutingContext context)
	{
		return this.sc;
	}

}
