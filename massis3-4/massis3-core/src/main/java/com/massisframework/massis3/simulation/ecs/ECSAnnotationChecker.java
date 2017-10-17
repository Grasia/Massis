package com.massisframework.massis3.simulation.ecs;

import java.lang.annotation.Annotation;

@SuppressWarnings("rawtypes")
public final class ECSAnnotationChecker {

	private ECSAnnotationChecker()
	{
	}

	private static boolean ENFORCE_ECS = true;

	public static void setEnforce_ecs(final boolean b)
	{
		ENFORCE_ECS = b;
	}

	public static boolean isECSEnforced()
	{
		return ENFORCE_ECS;
	}

	public static void ensureStateAllowed(
			final Class stateType,
			final Class<? extends Annotation> callType,
			final Class... componentsToCheck)
	{
		if (!isECSEnforced())
		{
			return;
		}

		final Class[] cmpTypes = getAllowedComponents(stateType, callType);
		for (final Class cmpType : componentsToCheck)
		{
			boolean isAllowed = false;
			for (int i = 0; i < cmpTypes.length; i++)
			{
				if (cmpTypes[i] == cmpType)
				{
					isAllowed = true;
					break;
				}
			}
			if (!isAllowed)
			{
				throw new IllegalArgumentException(
						"The annotation @" + callType.getSimpleName()
								+ " of " + stateType.getSimpleName()
								+ " does not contain "
								+ cmpType.getSimpleName());
			}
		}
	}

	public static Class[] getAllowedComponents(final Class<?> caller,
			final Class<? extends Annotation> callType)
	{
		final Annotation ann = caller.getAnnotation(callType);
		if (ann == null)
		{
			// throw new IllegalArgumentException(
			// "The caller must be annotated with @"
			// + callType.getName() + ". caller: "
			// + caller.getName());
			return new Class[] {};
		}
		try
		{
			return (Class[]) callType.getMethod("value").invoke(ann);
		} catch (final Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
