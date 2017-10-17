package com.massisframework.massis3.sposh.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.massisframework.massis3.sposh.Massis3Agent;
import com.massisframework.massis3.sposh.Massis3Context;

import cz.cuni.amis.pogamut.sposh.executor.ActionResult;
import cz.cuni.amis.pogamut.sposh.executor.Param;
import cz.cuni.amis.pogamut.sposh.executor.ParamsAction;
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
@PrimitiveInfo(name = "Write to Log", description = "Logs something")
public class LogAction<CONTEXT extends Massis3Context<Massis3Agent>> extends ParamsAction<CONTEXT> {

	private static final Logger log = LoggerFactory.getLogger(LogAction.class);

	/**
	 * Constructor of the action, used during automatic instantiation.
	 */
	public LogAction(CONTEXT ctx)
	{
		super(ctx);
	}

	/**
	 * Method responsible for initialization of the action. The method can be
	 * passed parameters from the plan. Add all desired plan parameters as
	 * method parameters, e.g. <tt>public void init({@literal @}Param("$speed")
	 * Integer runningSpeed)</tt>.
	 */
	public void init()
	{
	}

	/**
	 * Method called during each tick of the logic the action is supposed to
	 * run.
	 *
	 * The method can be passed parameters from the plan, e.g. <tt>public void
	 * run({@literal @}Param("$stuckTime") Double stuckTimeSecs)</tt>.
	 */
	public ActionResult run(@Param("$text") String text)
	{

		if (log.isInfoEnabled())
		{
			log.info("Log Action : {}", text);
		}
		return ActionResult.RUNNING_ONCE;
	}

	/**
	 * Method called once engine decides that another action should be run,
	 * place you cleanup code here.
	 *
	 * The method can be passed parameters from the plan, e.g. <tt>public void
	 * done({@literal @}Param("$notify") Boolean notifyTeam)</tt>.
	 */
	public void done()
	{
		// Add your cleanup code here
	}
}
