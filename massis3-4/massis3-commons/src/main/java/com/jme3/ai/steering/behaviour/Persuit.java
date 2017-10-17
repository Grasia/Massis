/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.ai.steering.behaviour;

import com.jme3.math.Vector3f;

/**
 * Pursuit is similar to seek except that the quarry (target) is another moving
 * character. Effective pursuit requires a prediction of the target’s future
 * position.
 * 
 * @author Brent Owens
 */
public class Persuit implements Behaviour {

	public Vector3f calculateForce(final Vector3f location,
			final Vector3f velocity,
			final float speed,
			final float targetSpeed,
			final float tpf,
			final Vector3f targetVelocity,
			final Vector3f targetLocation)
	{

		// calculate speed difference to see how far ahead we need to leed
		final float speedDiff = targetSpeed - speed;
		final float desiredSpeed = (targetSpeed + speedDiff) * tpf;
		final Vector3f projectedLocation = targetLocation
				.add(targetVelocity.mult(desiredSpeed));
		final Vector3f desierdVel = projectedLocation.subtract(location)
				.normalize().mult(speed);
		final Vector3f steering = desierdVel.subtract(velocity);

		return steering;
	}
}
