package com.massisframework.massis3.sposh.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
@PrimitiveInfo(
		name = "Stay Still",
		description = "Stays doing nothing during certain amount of time")
public class StayStill extends MassisAction {

	private static final Logger log = LoggerFactory.getLogger(StayStill.class);
	private float timeIdle;
	private float initialTime;

	/**
	 * Constructor of the action, used during automatic instantiation.
	 */
	public StayStill(Massis3Context<Massis3Agent> ctx)
	{
		super(ctx);
	}

	/**
	 * Method responsible for initialization of the action. The method can be
	 * passed parameters from the plan. Add all desired plan parameters as
	 * method parameters, e.g. <tt>public void init({@literal @}Param("$speed")
	 * Integer runningSpeed)</tt>.
	 */
	public void init(@Param("$time") int time)
	{
		this.timeIdle = time*1000L;
	}

	@Override
	protected void fistRun()
	{
		this.initialTime = this.agent().getTime();
	}

	@Override
	protected void logic()
	{
		float currentTime = this.agent().getTime();

		if (log.isInfoEnabled())
		{
			log.info("Initial time: {}. TimeIdle : {}. current time: {}", initialTime,timeIdle,currentTime);
		}

		if (currentTime - this.initialTime <= timeIdle)
		{
			this.setResult(ActionResult.RUNNING);
		} else
		{
			this.setResult(ActionResult.FINISHED);
		}
	}

}
