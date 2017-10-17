package com.massisframework.massis3.sposh.senses;

import com.jme3.math.Vector3f;
import com.massisframework.massis3.sposh.Massis3Agent;
import com.massisframework.massis3.sposh.Massis3Context;

import cz.cuni.amis.pogamut.sposh.executor.ParamsSense;
import cz.cuni.amis.pogamut.sposh.executor.PrimitiveInfo;

/**
 * Sense NewSense for Yaposh.
 *
 * @author rpax
 * @param <CONTEXT>
 *            Context class of the sense. It's an shared object used by all
 *            primitives. it is used as a shared memory and for interaction with
 *            the environment.
 */
@PrimitiveInfo(
		name = "Distance to target",
		description = "returns the distance of the agent to its target")
public class DistanceToTarget extends ParamsSense<Massis3Context<Massis3Agent>, Double> {

	/**
	 * Constructor of the sense, used during automatic instantiation. The class
	 * passed to the ancestor is used to determine which query method should be
	 * used by the sense.
	 */
	public DistanceToTarget(Massis3Context<Massis3Agent> ctx)
	{
		super(ctx, Double.class);
	}

	/**
	 * Query the current value of the sense. The sense can be passed parameters
	 * from the plan. Add all desired plan parameters as method parameters, e.g.
	 * <tt>public Boolean query({@literal @}Param("$name") String botName,
	 * {@literal @}Param("$threshold") Integer threshold)</tt>.
	 */
	public Double query()
	{
		return (double) this.getCtx().getBot().getPosition()
				.distance(this.ctx.getMovementTarget(new Vector3f()));
	}
}
