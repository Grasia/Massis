package com.massisframework.massis3.commons.json;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.jme3.math.Quaternion;

public class QuaternionSerializer
		implements JsonSerializer<Quaternion>, JsonDeserializer<Quaternion> {

	@Override
	public Quaternion deserialize(final JsonElement json, final Type typeOfT,
			final JsonDeserializationContext context) throws JsonParseException
	{
		final JsonArray arr = json.getAsJsonArray();
		final Quaternion v = new Quaternion(
				arr.get(0).getAsFloat(),
				arr.get(1).getAsFloat(),
				arr.get(2).getAsFloat(),
				arr.get(3).getAsFloat());
		return v;
	}

	@Override
	public JsonElement serialize(final Quaternion src, final Type typeOfSrc,
			final JsonSerializationContext context)
	{
		final JsonArray arr = new JsonArray();
		arr.add(src.getX());
		arr.add(src.getY());
		arr.add(src.getZ());
		arr.add(src.getW());
		return arr;
	}

}
