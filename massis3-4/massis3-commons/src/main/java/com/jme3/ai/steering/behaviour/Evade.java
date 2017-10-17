/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.ai.steering.behaviour;

import com.jme3.math.Vector3f;

/**
 * Evasion is analogous to pursuit, except that flee is used to steer away from
 * the predicted future position of the target character.
 * 
 * @author Brent Owens
 */
public class Evade {

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
		final Vector3f steering = desierdVel.subtract(velocity).negate(); // negate
																			// the
																			// direction

		return steering;
	}

}
