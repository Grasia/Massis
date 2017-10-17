package com.massisframework.massis3.services;

import java.io.PrintWriter;
import java.io.StringWriter;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class Responses {

	public static JsonObject jsonResponseOK(Object content)
	{
		return jsonResponse(content, false, -1, null, null);
	}

	public static JsonObject jsonResponseError(int errCode, String errMessage)
	{
		return jsonResponse(null, true, errCode, errMessage, null);
	}

	public static JsonObject jsonResponseError(int errCode, String errMessage, Throwable ex)
	{
		return jsonResponse(null, true, errCode, errMessage, ex);
	}

	private static JsonObject jsonResponse(
			Object content,
			final boolean error,
			final int errCode,
			final String errorMsg,
			final Throwable ex)
	{
		final JsonObject obj = new JsonObject();
		if (error)
		{
			obj.put("status", "error");
			obj.put("errorCode", errCode);
			obj.put("description", errorMsg);
			if (ex != null)
			{
				JsonObject exceptionDetails = new JsonObject();
				StringWriter sw = new StringWriter();
				ex.printStackTrace(new PrintWriter(sw));
				exceptionDetails.put("stacktrace", sw.getBuffer().toString());
				obj.put("exceptionDetails", exceptionDetails);
			}

		} else
		{
			obj.put("status", "ok");
			if (content != null)
			{
				if (

				!content.getClass().isPrimitive()
						&& !(content instanceof Boolean)
						&& !(content instanceof Iterable)
						&& !(content.getClass().isArray())
						&& !(content instanceof Number)
						&& !(content instanceof JsonArray)
						&& !(content instanceof String))
				{
					content = JsonObject.mapFrom(content);
				}
			}
			obj.put("result", content);
		}
		return obj;
	}
}
