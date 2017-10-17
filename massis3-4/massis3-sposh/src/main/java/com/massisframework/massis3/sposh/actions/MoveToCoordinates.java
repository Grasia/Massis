package com.massisframework.massis3.sposh.actions;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.math.Vector3f;
import com.massisframework.massis3.sposh.ContextKey;
import com.massisframework.massis3.sposh.Massis3Agent;
import com.massisframework.massis3.sposh.Massis3Context;
import com.massisframework.massis3.sposh.executor.MassisAction;

import cz.cuni.amis.pogamut.sposh.executor.ActionResult;
import cz.cuni.amis.pogamut.sposh.executor.PrimitiveInfo;
import io.vertx.core.json.JsonArray;

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
		name = "Move to target",
		description = "Moves to an specified place in coordinates")
public class MoveToCoordinates extends MassisAction {

	private static final Logger log = LoggerFactory.getLogger(MoveToCoordinates.class);

	private Vector3f target = new Vector3f(Float.NaN, Float.NaN, Float.NaN);
	private AtomicBoolean isFollowingPath = new AtomicBoolean(true);

	/**
	 * Constructor of the action, used during automatic instantiation.
	 */
	public MoveToCoordinates(Massis3Context<Massis3Agent> ctx)
	{
		super(ctx);
	}

	private AtomicBoolean followCalled = new AtomicBoolean(false);

	private void checkFollow()
	{
		if (followCalled.getAndSet(true))
			return;
		this.agent().isFollowingPath(r -> {
			if (r.failed())
			{
				log.error("Error when checking if a path was being followed", r.cause());
				this.setResult(ActionResult.FAILED);
			} else
			{
				this.isFollowingPath.set(r.result());
				followCalled.set(false);
			}
		});
	}

	/**
	 * Method responsible for initialization of the action. The method can be
	 * passed parameters from the plan. Add all desired plan parameters as
	 * method parameters, e.g. <tt>public void init({@literal @}Param("$speed")
	 * Integer runningSpeed)</tt>.
	 */
	public void init()
	{
		this.target = ((Vector3f) this.ctx.getContextValue(ContextKey.MOVEMENT_TARGET)).clone();
		this.isFollowingPath.set(true);
	}

	@Override
	protected void fistRun()
	{
		this.agent().moveTowards(this.target, res -> {
			if (res.failed())
			{
				log.error("Setting target in server failed", res.cause());
				this.getCtx().setContextValue(ContextKey.IS_MOVING, false);
				setResult(ActionResult.FAILED);
			} else
			{
				if (log.isInfoEnabled())
				{
					log.info("Target set in server");
				}

			}
		});
	}

	/**
	 * Method called during each tick of the logic the action is supposed to
	 * run.
	 *
	 * The method can be passed parameters from the plan, e.g. <tt>public void
	 * run({@literal @}Param("$stuckTime") Double stuckTimeSecs)</tt>.
	 */
	@Override
	public void logic()
	{

		// checkFollow();

		if (targetReached())
		{

			if (log.isInfoEnabled())
			{
				log.info("Target reached in client");
			}

			this.getCtx().setContextValue(ContextKey.IS_MOVING, false);
			this.setResult(ActionResult.FINISHED);
		} else
		{

			this.getCtx().setContextValue(ContextKey.IS_MOVING, true);
			this.setResult(ActionResult.RUNNING);
		}
	}

	private boolean targetReached()
	{
		return this.agent().getPosition().distance(this.target) < 3;
	}

	@Override
	protected void cleanup()
	{

		if (log.isInfoEnabled())
		{
			log.info("Cleaning move command..");
		}

		this.agent().stopMoving(r -> {
			if (r.failed())
			{
				log.error("Stop moving command failed");
			}
		});
	}

}
