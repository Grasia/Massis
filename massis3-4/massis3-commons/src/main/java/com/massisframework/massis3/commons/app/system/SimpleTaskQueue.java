package com.massisframework.massis3.commons.app.system;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.massisframework.massis3.commons.app.CompletableAppTask;

public class SimpleTaskQueue {

	private final ConcurrentLinkedQueue<CompletableAppTask<?>> taskQueue;

	public SimpleTaskQueue()
	{
		taskQueue = new ConcurrentLinkedQueue<>();
	}

	/**
	 * Enqueues a task/callable object to execute in the jME3 rendering thread.
	 * <p>
	 * Callables are executed right at the beginning of the main loop. They are
	 * executed even if the application is currently paused or out of focus.
	 *
	 * @param callable
	 *            The callable to run in the main jME3 thread
	 */
	public <V> CompletionStage<V> enqueue(Callable<V> callable)
	{
		CompletableAppTask<V> task = CompletableAppTask.of(callable);
		taskQueue.add(task);
		return task;
	}

	/**
	 * Enqueues a runnable object to execute in the jME3 rendering thread.
	 * <p>
	 * Runnables are executed right at the beginning of the main loop. They are
	 * executed even if the application is currently paused or out of focus.
	 *
	 * @param runnable
	 *            The runnable to run in the main jME3 thread
	 */
	private static final Object NIL = new Object();

	public void enqueue(Runnable runnable)
	{
		enqueue(() -> {
			runnable.run();
			return NIL;
		});
	}

	/**
	 * Runs tasks enqueued via {@link #enqueue(Callable)}
	 */
	public void runQueuedTasks()
	{
		CompletableAppTask<?> task;
		while ((task = taskQueue.poll()) != null)
		{
			if (!task.isCancelled())
			{
				task.execute();
			}
		}
	}
}
