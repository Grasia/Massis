package com.massisframework.massis3.services.dataobjects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
		"x",
		"y",
		"z"
})
@DataObject
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonQuaternion {

//	@JsonProperty("x")
	private float x;
//	@JsonProperty("y")
	private float y;
//	@JsonProperty("z")
	private float z;
//	@JsonProperty("w")
	private float w;

	public JsonQuaternion(JsonObject json)
	{
		this.x = (io.vertx.core.json.Json.mapper.convertValue(json.getValue("x"), float.class));
		this.y = (io.vertx.core.json.Json.mapper.convertValue(json.getValue("y"), float.class));
		this.z = (io.vertx.core.json.Json.mapper.convertValue(json.getValue("z"), float.class));
		this.z = (io.vertx.core.json.Json.mapper.convertValue(json.getValue("w"), float.class));
	}

	/**
	 * No args constructor for use in serialization
	 * 
	 */
	public JsonQuaternion()
	{
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public JsonQuaternion(float x, float y, float z,float w)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.w=w;
	}

	@JsonProperty("x")
	public float getX()
	{
		return x;
	}

	@JsonProperty("x")
	public void setX(float x)
	{
		this.x = x;
	}

	public JsonQuaternion withX(float x)
	{
		this.x = x;
		return this;
	}

	@JsonProperty("y")
	public float getY()
	{
		return y;
	}

	@JsonProperty("y")
	public void setY(float y)
	{
		this.y = y;
	}

	public JsonQuaternion withY(float y)
	{
		this.y = y;
		return this;
	}

	@JsonProperty("z")
	public float getZ()
	{
		return z;
	}

	@JsonProperty("z")
	public void setZ(float z)
	{
		this.z = z;
	}

	public JsonQuaternion withZ(float z)
	{
		this.z = z;
		return this;
	}

	@JsonProperty("w")
	public void setW(float w)
	{
		this.w = w;
	}

	public JsonQuaternion withW(float w)
	{
		this.w = w;
		return this;
	}

	@JsonProperty("w")
	public float getW()
	{
		return w;
	}
	
	public JsonObject toJson()
	{
		return (JsonObject.mapFrom(this));
	}

}
