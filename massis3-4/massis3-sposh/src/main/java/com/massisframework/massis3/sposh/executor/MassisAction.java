package com.massisframework.massis3.sposh.executor;

import java.util.concurrent.atomic.AtomicReference;

import com.massisframework.massis3.sposh.Massis3Agent;
import com.massisframework.massis3.sposh.Massis3Context;

import cz.cuni.amis.pogamut.sposh.executor.ActionResult;
import cz.cuni.amis.pogamut.sposh.executor.ParamsAction;

public abstract class MassisAction extends ParamsAction<Massis3Context<Massis3Agent>> {

	private AtomicReference<ActionResult> actionResult;

	private boolean firstRun = true;

	public MassisAction(Massis3Context<Massis3Agent> ctx)
	{
		super(ctx);
		this.actionResult = new AtomicReference<ActionResult>(ActionResult.RUNNING);
	}

	// protected <T> T getService(Class<T> serviceType)
	// {
	// return this.getCtx().getService(serviceType);
	// }

	protected long id()
	{
		return this.getCtx().getBot().getID();
	}

	protected Massis3Agent agent()
	{
		return this.getCtx().getBot();
	}

	public final ActionResult run()
	{

		if (this.firstRun)
		{
			this.firstRun = false;
			this.fistRun();
		} else
		{
			if (this.actionResult.get() != ActionResult.FAILED)
			{
				this.logic();
			}
		}
		return this.actionResult.get();
	}

	protected abstract void fistRun();

	protected void logic()
	{
	}

	// protected HumanAgentService humanService()
	// {
	// return this.getCtx().getService(HumanAgentService.class);
	// }
	// protected EnvironmentService envService()
	// {
	// return this.getCtx().getService(EnvironmentService.class);
	// }

	protected void setResult(ActionResult newValue)
	{
		if (this.actionResult.get() != ActionResult.FAILED)
		{
			this.actionResult.set(newValue);
		}
	}

	protected ActionResult getResult()
	{
		return this.actionResult.get();
	}

	protected void cleanup()
	{
	}

	public final void done()
	{
		this.cleanup();
		this.firstRun = true;
		this.actionResult.set(ActionResult.RUNNING);
	}

}
