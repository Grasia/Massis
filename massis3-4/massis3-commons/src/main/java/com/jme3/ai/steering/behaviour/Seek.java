/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.ai.steering.behaviour;

import com.jme3.math.Vector3f;

/**
 * Seek (or pursuit of a static target) acts to steer the character towards a
 * specified position in global space. This behavior adjusts the character so
 * that its velocity is radially aligned towards the target.
 * 
 * @author Brent Owens
 */
public class Seek implements Behaviour {

	public Vector3f calculateForce(final Vector3f location,
			final Vector3f velocity,
			final float speed, final Vector3f target)
	{

		final Vector3f desierdVel = target.subtract(location).normalize()
				.mult(speed);
		final Vector3f steering = desierdVel.subtract(velocity);

		return steering;
	}
}
