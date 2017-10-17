package com.massisframework.massis3.commons.spatials;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.List;

import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.util.TempVars;

public final class LinesUtils {

	private LinesUtils()
	{
	}

	public static float minimum_distance2D(final Vector3f v, final Vector3f w,
			final Vector3f p)
	{
		/**
		 * @formatter:off
		 */
		final TempVars tmp=TempVars.get();
		// Return minimum distance between line segment vw and point p
		// i.e. |w-v|^2 -  avoid a sqrt
		final float l2 = length2DSquared(w.subtract(v,tmp.vect1)); 
		//distance(p, v);   // v == w case
		if (FastMath.approximateEquals(l2, 0f)) {tmp.release();return distance2D(p, v);}
		// Consider the line extending the segment, parameterized as v + t (w - v).
		// We find projection of point p onto the line. 
		// It falls where t = [(p-v) . (w-v)] / |w-v|^2
		// We clamp t from [0,1] to handle points outside the segment vw.
		final float t = max(0, min(1, dot2D(p.subtract(v,tmp.vect2), w.subtract(v,tmp.vect3)) / l2));
//		const vec2 projection = v + t * (w - v);  // Projection falls on the segment
		// Projection falls on the segment
		final Vector3f projection = v.add(w.subtract(v,tmp.vect4).mult(t,tmp.vect5),tmp.vect6); 
		final float dist= distance2D(p, projection);
		tmp.release();
		 /**
		  * @formatter:on
		  */
		return dist;
	}

	public static float dot2D(final Vector3f a, final Vector3f b)
	{
		return a.x * b.x + a.z * b.z;
	}

	/**
	 * https://web.archive.org/save/_embed/http://stackoverflow.com/questions/
	 * 3120357/get-closest-point-to-a-line/9557244#9557244
	 * 
	 */
	public static Vector3f closestPointOnLineSegment(final Vector3f A,
			final Vector3f B,
			final Vector3f P)
	{
		final Vector3f result = new Vector3f();
		final TempVars tmp = TempVars.get();
		final Vector3f AP = P.subtract(A, tmp.vect1); // Vector from A to P
		final Vector3f AB = B.subtract(A, tmp.vect2); // Vector from A to B

		final float magnitudeAB = AB.lengthSquared(); // Magnitude of AB vector
		// (it's// length squared)
		final float ABAPproduct = AP.dot(AB); // The DOT product of a_to_p and
												// a_to_b
		final float distance = ABAPproduct / magnitudeAB; // The normalized
															// "distance"
		// from a to your closest
		// point

		if (distance < 0) // Check if P projection is over vectorAB
		{
			result.set(A);

		} else if (distance > 1)
		{
			result.set(B);
		} else
		{
			result.set(A.add(AB.mult(distance, tmp.vect3), tmp.vect4));
		}
		tmp.release();
		return result;
	}

	public static float pathLength(final List<Vector3f> path)
	{
		float length = 0;
		for (int i = 0; i < path.size() - 1; i++)
		{
			length += path.get(i).distance(path.get(i + 1));
		}
		return length;
	}

	public static Vector3f midPoint(final Vector3f a, final Vector3f b)
	{
		return new Vector3f(
				(a.x + b.x) / 2.0f,
				(a.y + b.y) / 2.0f,
				(a.z + b.z) / 2.0f);
	}

	public static float length2DSquared(final Vector3f v)
	{
		return v.x * v.x + v.z * v.z;
	}

	public static float length2D(final Vector3f v)
	{
		return FastMath.sqrt(length2DSquared(v));
	}

	/**
	 * TODO optimize
	 * 
	 * @param point
	 * @param path
	 * @return
	 */
	public static int nearestLineSegment(final Vector3f point,
			final List<Vector3f> path)
	{
		float minDist = Float.MAX_VALUE;
		int index = 0;
		for (int i = 0; i < path.size() - 1; i++)
		{
			final Vector3f a = path.get(i);
			final Vector3f b = path.get(i + 1);
			final Vector3f closest = closestPointOnLineSegment(a, b, point);
			final float distance = point.distance(closest);
			if (distance < minDist)
			{
				minDist = distance;
				index = i;
			}
		}
		return index;
	}

	public static int nearestLineSegment(final Vector3f point,
			final float radius,
			final List<Vector3f> path)
	{
		float minDist = Float.MAX_VALUE;
		int index = 0;
		for (int i = 0; i < path.size() - 1; i++)
		{
			final Vector3f a = path.get(i);
			final Vector3f b = path.get(i + 1);
			final Vector3f closest = closestPointOnLineSegment(a, b, point);
			final float distance = point.distance(closest);
			if (distance < minDist || distance < radius)
			{
				minDist = distance;
				index = i;
			}
		}
		return index;
	}

	public static float distance2DSq(final Vector3f a, final Vector3f b)
	{
		final double dx = a.x - b.x;
		final double dy = a.z - b.z;
		return (float) (dx * dx + dy * dy);
	}

	public static float distance2DSq(final Vector3f a, final Vector2f b)
	{
		final double dx = a.x - b.x;
		final double dy = a.z - b.y;
		return (float) (dx * dx + dy * dy);
	}

	public static float distance2D(final Vector3f a, final Vector3f b)
	{
		return FastMath.sqrt(distance2DSq(a, b));
	}

	public static float distance2D(final Vector3f a, final Vector2f b)
	{
		return FastMath.sqrt(distance2DSq(a, b));
	}

	public static Vector3f midPointPercentage(final Vector3f v0,
			final Vector3f v1, final float p)
	{
		return midPointPercentage(v0, v1, p, new Vector3f());
	}

	public static Vector3f midPointPercentage(final Vector3f v0,
			final Vector3f v1, final float p,
			final Vector3f store)
	{
		return store.set(
				(v1.x - v0.x) * p + v0.x,
				(v1.y - v0.y) * p + v0.y,
				(v1.z - v0.z) * p + v0.z);
	}

	public static Vector3f direction(final Vector3f a, final Vector3f b)
	{
		return b.subtract(a).normalizeLocal();
	}

	public static float velocityDistance(final Vector3f position,
			final float speed,
			final Vector3f targetPos, final float tpf)
	{

		final TempVars tmp = TempVars.get();
		//
		final Vector3f pos = tmp.vect1.set(position);
		final Vector3f vel = tmp.vect2
				.set(targetPos)
				.subtractLocal(pos)
				.normalizeLocal()
				.multLocal(speed);
		final Vector3f futurePos = pos.add(vel.mult(tpf, tmp.vect3), tmp.vect4);
		final float minDist = minimum_distance2D(pos, futurePos, targetPos);
		tmp.release();
		return minDist;
	}
}
