package com.massisframework.massis3.sposh.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.massisframework.massis3.sposh.ContextKey;
import com.massisframework.massis3.sposh.Massis3Agent;
import com.massisframework.massis3.sposh.Massis3Context;
import com.massisframework.massis3.sposh.executor.MassisAction;

import cz.cuni.amis.pogamut.sposh.executor.ActionResult;
import cz.cuni.amis.pogamut.sposh.executor.Param;
import cz.cuni.amis.pogamut.sposh.executor.PrimitiveInfo;

/**
 * Action Animate for Yaposh.
 *
 * @author rpax
 * @param <CONTEXT>
 *            Context class of the action. It's an shared object used by all
 *            primitives. it is used as a shared memory and for interaction with
 *            the environment.
 */
@PrimitiveInfo(name = "Animate", description = "Description of Animate")
public class Animate extends MassisAction {

	private static final Logger log = LoggerFactory.getLogger(Animate.class);
	private String animation;

	/**
	 * Constructor of the action, used during automatic instantiation.
	 */
	public Animate(Massis3Context<Massis3Agent> ctx)
	{
		super(ctx);
	}

	private String getCurrentAnim()
	{
		return this.getCtx().getContextValue(ContextKey.CURRENT_ANIMATION, "none");
	}

	private void setCurrentAnim(String animName)
	{

		this.getCtx().setContextValue(ContextKey.CURRENT_ANIMATION,
				animName.replace(".massisanim", ""));
	}

	/**
	 * Method responsible for initialization of the action. The method can be
	 * passed parameters from the plan. Add all desired plan parameters as
	 * method parameters, e.g. <tt>public void init({@literal @}Param("$speed")
	 * Integer runningSpeed)</tt>.
	 */
	public void init(@Param("$animation") String animation)
	{
		if (!animation.endsWith(".massisanim"))
		{
			animation = animation + ".massisanim";
		}
		this.animation = animation;
	}

	@Override
	protected void fistRun()
	{
		if (this.getCurrentAnim().equals(animation))
		{
			setResult(ActionResult.FINISHED);
		}
		this.setCurrentAnim(animation);
		this.agent().animate(animation, true, res -> {
			if (res.failed())
			{
				log.error("Error when executing animation", res.cause());
				setResult(ActionResult.FAILED);
			} else
			{
				setResult(ActionResult.FINISHED);
			}
		});

	}
}
