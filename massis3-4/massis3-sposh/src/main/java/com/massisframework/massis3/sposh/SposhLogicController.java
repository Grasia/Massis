package com.massisframework.massis3.sposh;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import cz.cuni.amis.pogamut.sposh.context.Context;
import cz.cuni.amis.pogamut.sposh.elements.PoshPlan;
import cz.cuni.amis.pogamut.sposh.engine.PoshEngine;
import cz.cuni.amis.pogamut.sposh.engine.timer.SystemClockTimer;
import cz.cuni.amis.pogamut.sposh.executor.IAction;
import cz.cuni.amis.pogamut.sposh.executor.ISense;

@SuppressWarnings("rawtypes")
public class SposhLogicController {

	private static AtomicInteger ENGINE_COUNT = new AtomicInteger(0);
	private Massis3Context<? extends Massis3Agent> ctx;
	private PoshPlan poshPlan;
	private Massis3StateWorkExecutor executor;
	private SystemClockTimer timer;
	private PoshEngine poshEngine;
	private static final java.util.logging.Logger poshEngineLogger = java.util.logging.Logger
			.getLogger(SposhLogicController.class.getName());
	private AtomicBoolean stopped = new AtomicBoolean();

	public SposhLogicController(Massis3Context<? extends Massis3Agent> ctx, PoshPlan poshPlan)
	{
		this.poshPlan = poshPlan;
		this.executor = new Massis3StateWorkExecutor();
		this.timer = new SystemClockTimer();
		this.ctx = ctx;
		for (String actionName : this.poshPlan.getActionsNames())
		{
			IAction actionInstance = createPrimitiveAction(actionName, this.ctx);
			executor.addAction(actionInstance.getClass().getName(), actionInstance);
		}
		for (String senseName : this.poshPlan.getSensesNames())
		{
			ISense senseInstance = createPrimitiveSense(senseName, this.ctx);
			executor.addSense(senseInstance.getClass().getName(), senseInstance);
		}
		this.poshEngine = new PoshEngine(ENGINE_COUNT.getAndIncrement(), this.poshPlan, this.timer,
				poshEngineLogger);
	}

	public void logic()
	{
		if (!stopped.get())
		{
			this.poshEngine.evaluatePlan(this.executor);
		}
	}

	public void cleanup()
	{
		stopped.set(true);
	}

	private static IAction createPrimitiveAction(String actionName, Context ctx)
	{
		return (IAction) newInstance(actionName, ctx);
	}

	private static ISense createPrimitiveSense(String senseName, Context ctx)
	{
		return (ISense) newInstance(senseName, ctx);
	}

	private static <T> T newInstance(String typeName, Context ctx)
	{

		try
		{
			Class<T> type = (Class<T>) Class.forName(typeName);

			for (Constructor<?> ctor : type.getConstructors())
			{
				Parameter[] params = ctor.getParameters();
				if (params.length == 1 && Context.class.isAssignableFrom(params[0].getType()))
				{
					return (T) ctor.newInstance(ctx);

				}
			}
			// try with default ctor
			return type.newInstance();
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}

	}

	private static <T> Constructor<T> getCtor(Class<T> type, Class... params)
	{
		try
		{
			return type.getConstructor(params);
		} catch (NoSuchMethodException | SecurityException e)
		{
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> Class<T> forName(String typeName)
	{
		try
		{
			return (Class<T>) Class.forName(typeName);
		} catch (ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}

}
