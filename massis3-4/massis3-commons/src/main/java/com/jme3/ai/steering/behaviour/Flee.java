/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.ai.steering.behaviour;

import com.jme3.math.Vector3f;

/**
 * Flee is simply the inverse of seek and acts to steer the character so that
 * its velocity is radially aligned away from the target. The desired velocity
 * points in the opposite direction.
 * 
 * @author Brent Owens
 */
public class Flee implements Behaviour {

	public Vector3f calculateForce(final Vector3f location,
			final Vector3f velocity,
			final float speed, final Vector3f target)
	{

		final Vector3f desierdVel = target.subtract(location).normalize()
				.mult(speed);
		final Vector3f steering = desierdVel.subtract(velocity).negate(); // negate
																			// flee

		return steering;
	}
}
