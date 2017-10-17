package com.massisframework.massis3.web.injectors;

import com.github.aesteve.vertx.nubes.exceptions.params.MandatoryParamException;
import com.github.aesteve.vertx.nubes.exceptions.params.WrongParameterException;
import com.github.aesteve.vertx.nubes.exceptions.params.WrongParameterException.ParamType;
import com.github.aesteve.vertx.nubes.reflections.injectors.annot.AnnotatedParamInjector;
import com.massisframework.massis3.services.eventbus.Massis3ServiceUtils;

import io.vertx.ext.web.RoutingContext;

public class EventBusServiceInjector implements AnnotatedParamInjector<EventBusSimulationService> {

	@Override
	public Object resolve(
			RoutingContext context,
			EventBusSimulationService annotation,
			String paramName, Class<?> resultClass) throws WrongParameterException
	{
		String group = annotation.group();
		boolean literal = annotation.literal();
		if (!literal)
		{

			final String paramValue = context.request().getParam(group);
			if (paramValue == null)
			{
				throw new MandatoryParamException(ParamType.REQUEST_PARAM, group);
			}
			return Massis3ServiceUtils.createProxy(context.vertx(), resultClass, paramValue);
		}

		return Massis3ServiceUtils.createProxy(context.vertx(), resultClass, group);
	}

}
