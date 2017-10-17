package com.massisframework.massis3.commons.json;

import java.lang.reflect.Type;

import org.reflections.ReflectionUtils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class JsonClassSerializer
		implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>> {

	private final ClassAliasProvider prov;

	public JsonClassSerializer()
	{
		this(forNameProvider());
	}
	public JsonClassSerializer(final ClassAliasProvider prov)
	{
		this.prov = prov;
	}

	@Override
	public Class<?> deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) throws JsonParseException
	{
		return prov.getClassFromAlias(json.getAsString());
	}

	@Override
	public JsonElement serialize(final Class<?> src, final Type typeOfSrc,
			final JsonSerializationContext context)
	{
		return new JsonPrimitive(prov.getClassAlias(src));
	}
	
	public static ClassAliasProvider forNameProvider() {
		return new ClassAliasProvider() {
			
			@Override
			public Class<?> getClassFromAlias(String alias)
			{
				return ReflectionUtils.forName(alias);
			}
			
			@Override
			public String getClassAlias(Class<?> c)
			{
				return c.getName();
			}
		};
	}

}
