package com.massisframework.massis3.sposh.util;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class GeometryUtils {

	public static Vector3f randomPointInPolygon(JsonArray point3Array)
	{
		Vector3f min = new Vector3f();
		Vector3f max = new Vector3f();
		point3Array.stream().map(JsonObject.class::cast).forEach(point -> {
			min.x = Math.min(point.getFloat("x"), min.x);
			min.y = Math.min(point.getFloat("y"), min.y);
			min.z = Math.min(point.getFloat("z"), min.z);

			max.x = Math.max(point.getFloat("x"), max.x);
			max.y = Math.max(point.getFloat("y"), max.y);
			max.z = Math.max(point.getFloat("z"), max.z);
		});

		float x, z;
		do
		{
			x = min.x + (max.x - min.x) * FastMath.nextRandomFloat();
			z = min.z + (max.z - min.z) * FastMath.nextRandomFloat();
		} while (!pointInPolygon(point3Array, x, z));

		return new Vector3f(x, min.y, z);

	}

	// https://stackoverflow.com/a/8721483/3315914
	public static boolean pointInPolygon(JsonArray point3Array, float x, float z)
	{
		int i;
		int j;
		boolean result = false;

		for (i = 0, j = point3Array.size() - 1; i < point3Array.size(); j = i++)
		{
			if ((point3Array.getJsonObject(i).getFloat("z") > z) != (point3Array.getJsonObject(j)
					.getFloat("z") > z) &&
					(x < (point3Array.getJsonObject(j).getFloat("x")- point3Array.getJsonObject(i).getFloat("x"))
							* (z - point3Array.getJsonObject(i).getFloat("z"))
							/ (point3Array.getJsonObject(j).getFloat("z")
									- point3Array.getJsonObject(i).getFloat("z"))
							+ point3Array.getJsonObject(i).getFloat("x")))
			{
				result = !result;
			}
		}
		return result;
	}
}
