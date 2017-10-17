package com.massisframework.massis3.services.sockjs.impl;

import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.json.JsonObject;

public class SockJsProxyFrameHelper {

	private static final String ADDRESS = "address";
	private static final String REPLY_ADDRESS = "replyAddress";
	private static final String HEADERS = "headers";
	private static final String BODY = "body";

	private static final String TYPE = "type";
	private static final String SEND = "send";
	private static final String REGISTER = "register";
	private static final String PUBLISH = "publish";
	private static final String PING = "ping";
	private static final String ERR = "err";
	private static final String REC = "rec";

	public static void setSendType(boolean isSend, JsonObject store)
	{
		store.put(TYPE, isSend ? SEND : PUBLISH);
	}

	public static void setBody(Object body, JsonObject payload)
	{
		payload.put(BODY, body);
	}

	public static JsonObject ping()
	{
		return new JsonObject().put(TYPE, PING);
	}

	public static void setAddress(String address, JsonObject payload)
	{
		payload.put(ADDRESS, address);
	}

	public static String getAddress(JsonObject payload)
	{
		return payload.getString(ADDRESS);
	}

	public static void setHeaders(MultiMap headers, JsonObject payload)
	{
		if (headers != null)
		{
			JsonObject jsonHeaders = new JsonObject();
			headers.forEach(e -> jsonHeaders.put(e.getKey(), e.getValue()));
			payload.put(HEADERS, jsonHeaders);
		}

	}

	public static MultiMap getHeaders(JsonObject payload)
	{
		CaseInsensitiveHeaders headers = new CaseInsensitiveHeaders();
		payload.getJsonObject(HEADERS).forEach(e -> headers.add(e.getKey(), (String) e.getValue()));
		return headers;
	}

	public static void setReplyAddress(String replyAddress, JsonObject payload)
	{
		payload.put(REPLY_ADDRESS, replyAddress);
	}

	public static String getReplyAddress(JsonObject payload)
	{
		return payload.getString(REPLY_ADDRESS);
	}

	public static boolean hasReplyAddress(JsonObject payload)
	{
		return payload.containsKey(REPLY_ADDRESS);
	}

	public static String getType(JsonObject payload)
	{
		return payload.getString(TYPE);
	}

	public static boolean isError(JsonObject payload)
	{
		return ERR.equals(payload.getString(TYPE));
	}

	public static String getMessage(JsonObject data)
	{
		return data.getString("message");
	}

	public static Object getBody(JsonObject data)
	{
		return data.getValue(BODY);
	}

}
