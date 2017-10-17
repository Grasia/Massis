package com.massisframework.massis3.commons.steering;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.List;

import com.jme3.math.Vector2f;

public class SteeringUtil {

	public static Vector2f follow(
			final Vector2f position,
			final Vector2f velocity,
			final float frames_ahead,
			final float radius,
			final float maxspeed,
			final float maxforce,
			final List<Vector2f> points)
	{

		// Predict position 25 (arbitrary choice) frames ahead
		final Vector2f predict = velocity.clone();
		predict.normalizeLocal();
		// HARDCODED
		predict.multLocal(frames_ahead);
		// PVector.add(position,predict);
		final Vector2f predictpos = position.add(predict);

		// Now we must find the normal to the path from the predicted position
		// We look at the normal for each line segment and pick out the closest
		// one
		Vector2f normal = null;
		Vector2f target = null;
		// Start with a very high worldRecord
		// distance that can easily be beaten
		float worldRecord = 1000000;

		// Loop through all points of the path
		for (int i = 0; i < points.size() - 1; i++)
		{

			// Look at a line segment
			Vector2f a = points.get(i);
			// TODO maynot
			Vector2f b = points.get(i + 1);

			// Get the normal point to that line
			Vector2f normalPoint = getNormalPoint(predictpos, a, b);

			// Check if normal is on line segment
			Vector2f dir = b.subtract(a);// PVector.sub(b, a);
			// If it's not within the line segment, consider the normal to just
			// be the end of the line segment (point b)
			// if (da + db > line.mag()+1) {
			if (i + 2 < points.size())
			{
				if (normalPoint.x < min(a.x, b.x)
						|| normalPoint.x > max(a.x, b.x)
						|| normalPoint.y < min(a.y, b.y)
						|| normalPoint.y > max(a.y, b.y))
				{
					normalPoint = b.clone();
					// If we're at the end we really want the next line segment
					// for
					// looking ahead
					a = points.get(i + 1);
					b = points.get(i + 2); // Path wraps
											// around
					dir = b.subtract(a);// PVector.sub(b, a);
				}
			}
			// How far away are we from the path?
			final float d = predictpos.distance(normalPoint);// PVector.dist(predictpos,
			// normalPoint);
			// Did we beat the worldRecord and find the closest line segment?
			if (d < worldRecord)
			{
				worldRecord = d;
				normal = normalPoint;

				// Look at the direction of the line segment so we can seek a
				// little bit ahead of the normal
				dir.normalizeLocal();
				// This is an oversimplification
				// Should be based on distance to path & velocity
				dir.multLocal(frames_ahead);
				target = normal.clone();
				target.addLocal(dir);

			}
		}

		if (target == null)
		{
			return points.get(points.size() - 1).subtract(velocity);
		}

		// Only if the distance is greater than the path's radius do we bother
		// to steer
		if (worldRecord > radius)
		{
			// return velocity
			// .add(seek(position, velocity, maxspeed, maxforce, target));
			return target.subtract(position);
		} else
		{
			return velocity;
		}
	}

	// A method that calculates and applies a steering force towards a target
	// STEER = DESIRED MINUS VELOCITY
	private static Vector2f seek(final Vector2f position,
			final Vector2f velocity,
			final float maxspeed, final float maxforce, final Vector2f target)
	{
		// A vector pointing from the position to the target
		// PVector.sub(target, position);
		final Vector2f desired = target.subtract(position);

		// Normalize desired and scale to maximum speed
		desired.normalizeLocal();
		desired.multLocal(maxspeed);
		// Steering = Desired minus Vepositionity
		// PVector.sub(desired, velocity);
		final Vector2f steer = desired.subtract(velocity);
		// Limit to maximum steering force
		// steer.limit(maxforce);
		if (steer.length() > maxforce)
		{
			steer.normalizeLocal().multLocal(maxforce);
		}

		return steer;
	}

	// A function to get the normal point from a point (p) to a line segment
	// (a-b)
	// This function could be optimized to make fewer new Vector objects
	public static Vector2f getNormalPoint(final Vector2f p, final Vector2f a,
			final Vector2f b)
	{
		// Vector from a to p
		final Vector2f ap = p.subtract(a);// PVector.sub(p, a);
		// Vector from a to b
		final Vector2f ab = b.subtract(a);// PVector.sub(b, a);
		ab.normalizeLocal(); // Normalize the line
		// Project vector "diff" onto line by using the dot product
		ab.multLocal(ap.dot(ab));
		final Vector2f normalPoint = a.add(ab);// PVector.add(a, ab);
		return normalPoint;
	}

}
