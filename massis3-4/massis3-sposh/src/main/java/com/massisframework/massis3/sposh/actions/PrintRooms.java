package com.massisframework.massis3.sposh.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.massisframework.massis3.sposh.Massis3Agent;
import com.massisframework.massis3.sposh.Massis3Context;
import com.massisframework.massis3.sposh.executor.MassisAction;

import cz.cuni.amis.pogamut.sposh.executor.ActionResult;
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
@PrimitiveInfo(name = "Print Rooms", description = "Prints rooms")
public class PrintRooms extends MassisAction {

	private static final Logger log = LoggerFactory.getLogger(PrintRooms.class);

	/**
	 * Constructor of the action, used during automatic instantiation.
	 */
	public PrintRooms(Massis3Context<Massis3Agent> ctx)
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

	@Override
	protected void fistRun()
	{
		this.agent().getSceneRooms(res -> {
			if (res.failed()) {
				log.error("Error when retrieving rooms", res.cause());
				setResult(ActionResult.FAILED);
			}
			else {
				System.out.println(res.result().encodePrettily());
			}
		});
	}
}
