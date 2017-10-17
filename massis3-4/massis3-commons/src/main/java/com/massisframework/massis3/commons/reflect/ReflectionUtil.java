package com.massisframework.massis3.commons.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public final class ReflectionUtil {

	private ReflectionUtil()
	{
	}

	public static Class getCallerClass(final int arg0)
	{
		// getCallerClass
		return getCallerClassCallerSensitive(arg0 + 1);
	}

	@sun.reflect.CallerSensitive
	private static Class getCallerClassCallerSensitive(final int arg0)
	{
		final Class<?> caller = sun.reflect.Reflection.getCallerClass(arg0 + 2);
		return caller;
	}

	public static <T> T getFieldValue(Field f, Object obj)
	{
		try
		{
			return (T) f.get(obj);
		} catch (IllegalArgumentException | IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static <T> T getStaticFieldValue(Field f)
	{
		try
		{
			return (T) f.get(null);
		} catch (IllegalArgumentException | IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
	}
	public static boolean isStatic(Field f)
	{
		return Modifier.isStatic(f.getModifiers());
	}
	public static <C> C newInstance(Class<C> type)
	{
		try
		{
			return type.newInstance();
		} catch (InstantiationException | IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
	}

}
