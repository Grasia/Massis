package com.massisframework.massis3.commons.json;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.simsilica.es.EntityId;

public class EntityIdSerializer
		implements JsonSerializer<EntityId>, JsonDeserializer<EntityId> {

	@Override
	public EntityId deserialize(final JsonElement json, final Type typeOfT,
			final JsonDeserializationContext context) throws JsonParseException
	{
		return new EntityId(json.getAsLong());
	}

	@Override
	public JsonElement serialize(final EntityId src, final Type typeOfSrc,
			final JsonSerializationContext context)
	{
		return new JsonPrimitive(src.getId());
	}

}
