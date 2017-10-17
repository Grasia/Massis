package com.massisframework.massis3.services.dataobjects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.core.json.JsonObject;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
		"camId"
})
@DataObject
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetCameraDataMessage {

	
	private String camId;

	public GetCameraDataMessage(JsonObject json)
	{
		if (json.containsKey("camId"))
		{
			this.camId = (io.vertx.core.json.Json.mapper.convertValue(json.getValue("camId"),
					java.lang.String.class));
		}
	}

	/**
	 * No args constructor for use in serialization
	 * 
	 */
	public GetCameraDataMessage()
	{
	}

	/**
	 * 
	 * @param name
	 */
	public GetCameraDataMessage(String camId)
	{
		this.camId = camId;
	}

	@JsonProperty("camId")
	public String getCamId()
	{
		return camId;
	}

	@JsonProperty("camId")
	public void setCamId(String camId)
	{
		this.camId = camId;
	}

	public GetCameraDataMessage withCamId(String camId)
	{
		this.camId = camId;
		return this;
	}

	public JsonObject toJson()
	{
		return (JsonObject.mapFrom(this));
	}

}
