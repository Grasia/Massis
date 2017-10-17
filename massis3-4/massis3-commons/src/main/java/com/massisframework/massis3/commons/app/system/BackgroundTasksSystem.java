package com.massisframework.massis3.commons.app.system;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackgroundTasksSystem extends AbstractMassisSystem {

	private ExecutorService executor;
	private SimpleTaskQueue simpleTaskQueue = new SimpleTaskQueue();
	private static final Logger log = LoggerFactory.getLogger(BackgroundTasksSystem.class);

	@Override
	protected void simpleInitialize()
	{
		this.executor = Executors.newCachedThreadPool();
	}

	public <R> CompletionStage<R> enqueueInExecutor(Callable<R> task)
	{
		CompletableFuture<R> cF = new CompletableFuture<R>();
		this.executor.submit(() -> {
			try
			{
				R r = task.call();
				cF.complete(r);
			} catch (Exception ex)
			{
				cF.completeExceptionally(ex);
			}

		});
		return cF;
	}

	public <R> void enqueueInExecutor(Runnable task)
	{
		this.simpleTaskQueue.enqueue(task);
	}

	public <R> CompletionStage<R> enqueueInUpdate(Callable<R> task)
	{
		return this.simpleTaskQueue.enqueue(task);
	}

	@Override
	public void update()
	{
		this.simpleTaskQueue.runQueuedTasks();
	}

	@Override
	public void simpleCleanup()
	{
		this.executor.shutdownNow();
		try
		{
			this.executor.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e)
		{
			log.error("Executor interrupting while awaiting for task termination", e);
		}
	}

	@Override
	protected void onDisable()
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void onEnable()
	{
		// TODO Auto-generated method stub

	}

}
