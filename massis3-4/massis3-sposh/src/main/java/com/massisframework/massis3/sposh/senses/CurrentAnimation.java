package com.massisframework.massis3.sposh.senses;

import com.massisframework.massis3.sposh.ContextKey;
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
		name = "Current animation",
		description = "returns the animation that is being executed")
public class CurrentAnimation extends ParamsSense<Massis3Context<Massis3Agent>, String> {

	/**
	 * Constructor of the sense, used during automatic instantiation. The class
	 * passed to the ancestor is used to determine which query method should be
	 * used by the sense.
	 */
	public CurrentAnimation(Massis3Context<Massis3Agent> ctx)
	{
		super(ctx, String.class);
	}

	/**
	 * Query the current value of the sense. The sense can be passed parameters
	 * from the plan. Add all desired plan parameters as method parameters, e.g.
	 * <tt>public Boolean query({@literal @}Param("$name") String botName,
	 * {@literal @}Param("$threshold") Integer threshold)</tt>.
	 */
	public String query()
	{
		System.out.println("CURRENT ANIMATION: "+this.ctx.getContextValue(ContextKey.CURRENT_ANIMATION,"none"));
		return this.ctx.getContextValue(ContextKey.CURRENT_ANIMATION,"none");
	}
}
