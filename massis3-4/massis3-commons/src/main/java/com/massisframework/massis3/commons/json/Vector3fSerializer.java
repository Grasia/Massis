package com.massisframework.massis3.commons.json;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.jme3.math.Vector3f;

public class Vector3fSerializer
		implements JsonSerializer<Vector3f>, JsonDeserializer<Vector3f> {

	@Override
	public Vector3f deserialize(final JsonElement json, final Type typeOfT,
			final JsonDeserializationContext context) throws JsonParseException
	{
		final JsonArray arr = json.getAsJsonArray();
		final Vector3f v = new Vector3f(arr.get(0).getAsFloat(),
				arr.get(1).getAsFloat(), arr.get(2).getAsFloat());
		return v;
	}

	@Override
	public JsonElement serialize(final Vector3f src, final Type typeOfSrc,
			final JsonSerializationContext context)
	{
		final JsonArray arr = new JsonArray();
		arr.add(src.x);
		arr.add(src.y);
		arr.add(src.z);
		return arr;
	}

}
