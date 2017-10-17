package com.massisframework.massis3.sposh.actions;

import com.jme3.math.Vector3f;
import com.massisframework.massis3.sposh.ContextKey;
import com.massisframework.massis3.sposh.Massis3Agent;
import com.massisframework.massis3.sposh.Massis3Context;
import com.massisframework.massis3.sposh.executor.MassisAction;

import cz.cuni.amis.pogamut.sposh.executor.ActionResult;
import cz.cuni.amis.pogamut.sposh.executor.Param;
import cz.cuni.amis.pogamut.sposh.executor.PrimitiveInfo;

/**
 * Action Echo for Yaposh.
 *
 * @author rpax
 * @param <CONTEXT>
 *            Context class of the action. It's an shared object used by all
 *            primitives. it is used as a shared memory and for interaction with
 *            the environment.
 */
@PrimitiveInfo(
		name = "Set target coordinates",
		description = "Specifies the coordinates of the target. This does not make the agent move.")
public class SetMovementTargetCoordinates extends MassisAction {

	private float x;
	private float y;
	private float z;

	/**
	 * Constructor of the action, used during automatic instantiation.
	 */
	public SetMovementTargetCoordinates(Massis3Context<Massis3Agent> ctx)
	{
		super(ctx);
	}

	/**
	 * Method responsible for initialization of the action. The method can be
	 * passed parameters from the plan. Add all desired plan parameters as
	 * method parameters, e.g. <tt>public void init({@literal @}Param("$speed")
	 * Integer runningSpeed)</tt>.
	 */
	public void init(@Param("$x") double x, @Param("$y") double y, @Param("$z") double z)
	{
		this.x = (float) x;
		this.y = (float) y;
		this.z = (float) z;
	}

	@Override
	protected void fistRun()
	{
		this.getCtx().setContextValue(ContextKey.MOVEMENT_TARGET, new Vector3f(x, y, z));
		setResult(ActionResult.FINISHED);
	}

	@Override
	public void logic()
	{

	}

}
