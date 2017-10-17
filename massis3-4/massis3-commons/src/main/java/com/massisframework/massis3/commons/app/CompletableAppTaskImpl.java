package com.massisframework.massis3.commons.app;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class CompletableAppTaskImpl<T> implements CompletableAppTask<T> {

	private CompletableFuture<T> cF;
	private Callable<T> task;

	public CompletableAppTaskImpl(Callable<T> task)
	{
		this.task = task;
		this.cF = new CompletableFuture<>();
	}

	@Override
	public void execute()
	{
		if (!this.cF.isCancelled())
		{
			try
			{
				T r = this.task.call();
				this.cF.complete(r);
			} catch (Exception e)
			{
				this.cF.completeExceptionally(e);
			}
		}
	}

	@Override
	public boolean isDone()
	{
		return cF.isDone();
	}

	@Override
	public T get() throws InterruptedException, ExecutionException
	{
		return cF.get();
	}

	@Override
	public T get(long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException
	{
		return cF.get(timeout, unit);
	}

	@Override
	public <U> CompletableFuture<U> thenApply(Function<? super T, ? extends U> fn)
	{
		return cF.thenApply(fn);
	}

	@Override
	public <U> CompletableFuture<U> thenApplyAsync(Function<? super T, ? extends U> fn)
	{
		return cF.thenApplyAsync(fn);
	}

	@Override
	public <U> CompletableFuture<U> thenApplyAsync(Function<? super T, ? extends U> fn,
			Executor executor)
	{
		return cF.thenApplyAsync(fn, executor);
	}

	@Override
	public CompletableFuture<Void> thenAccept(Consumer<? super T> action)
	{
		return cF.thenAccept(action);
	}

	@Override
	public CompletableFuture<Void> thenAcceptAsync(Consumer<? super T> action)
	{
		return cF.thenAcceptAsync(action);
	}

	@Override
	public CompletableFuture<Void> thenAcceptAsync(Consumer<? super T> action, Executor executor)
	{
		return cF.thenAcceptAsync(action, executor);
	}

	@Override
	public CompletableFuture<Void> thenRun(Runnable action)
	{
		return cF.thenRun(action);
	}

	@Override
	public CompletableFuture<Void> thenRunAsync(Runnable action)
	{
		return cF.thenRunAsync(action);
	}

	@Override
	public CompletableFuture<Void> thenRunAsync(Runnable action, Executor executor)
	{
		return cF.thenRunAsync(action, executor);
	}

	@Override
	public <U, V> CompletableFuture<V> thenCombine(CompletionStage<? extends U> other,
			BiFunction<? super T, ? super U, ? extends V> fn)
	{
		return cF.thenCombine(other, fn);
	}

	@Override
	public <U, V> CompletableFuture<V> thenCombineAsync(CompletionStage<? extends U> other,
			BiFunction<? super T, ? super U, ? extends V> fn)
	{
		return cF.thenCombineAsync(other, fn);
	}

	@Override
	public <U, V> CompletableFuture<V> thenCombineAsync(CompletionStage<? extends U> other,
			BiFunction<? super T, ? super U, ? extends V> fn, Executor executor)
	{
		return cF.thenCombineAsync(other, fn, executor);
	}

	@Override
	public <U> CompletableFuture<Void> thenAcceptBoth(CompletionStage<? extends U> other,
			BiConsumer<? super T, ? super U> action)
	{
		return cF.thenAcceptBoth(other, action);
	}

	@Override
	public <U> CompletableFuture<Void> thenAcceptBothAsync(CompletionStage<? extends U> other,
			BiConsumer<? super T, ? super U> action)
	{
		return cF.thenAcceptBothAsync(other, action);
	}

	@Override
	public <U> CompletableFuture<Void> thenAcceptBothAsync(CompletionStage<? extends U> other,
			BiConsumer<? super T, ? super U> action, Executor executor)
	{
		return cF.thenAcceptBothAsync(other, action, executor);
	}

	@Override
	public CompletableFuture<Void> runAfterBoth(CompletionStage<?> other, Runnable action)
	{
		return cF.runAfterBoth(other, action);
	}

	@Override
	public CompletableFuture<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action)
	{
		return cF.runAfterBothAsync(other, action);
	}

	@Override
	public CompletableFuture<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action,
			Executor executor)
	{
		return cF.runAfterBothAsync(other, action, executor);
	}

	@Override
	public <U> CompletableFuture<U> applyToEither(CompletionStage<? extends T> other,
			Function<? super T, U> fn)
	{
		return cF.applyToEither(other, fn);
	}

	@Override
	public <U> CompletableFuture<U> applyToEitherAsync(CompletionStage<? extends T> other,
			Function<? super T, U> fn)
	{
		return cF.applyToEitherAsync(other, fn);
	}

	@Override
	public <U> CompletableFuture<U> applyToEitherAsync(CompletionStage<? extends T> other,
			Function<? super T, U> fn, Executor executor)
	{
		return cF.applyToEitherAsync(other, fn, executor);
	}

	@Override
	public CompletableFuture<Void> acceptEither(CompletionStage<? extends T> other,
			Consumer<? super T> action)
	{
		return cF.acceptEither(other, action);
	}

	@Override
	public CompletableFuture<Void> acceptEitherAsync(CompletionStage<? extends T> other,
			Consumer<? super T> action)
	{
		return cF.acceptEitherAsync(other, action);
	}

	@Override
	public CompletableFuture<Void> acceptEitherAsync(CompletionStage<? extends T> other,
			Consumer<? super T> action, Executor executor)
	{
		return cF.acceptEitherAsync(other, action, executor);
	}

	@Override
	public CompletableFuture<Void> runAfterEither(CompletionStage<?> other, Runnable action)
	{
		return cF.runAfterEither(other, action);
	}

	@Override
	public CompletableFuture<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action)
	{
		return cF.runAfterEitherAsync(other, action);
	}

	@Override
	public CompletableFuture<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action,
			Executor executor)
	{
		return cF.runAfterEitherAsync(other, action, executor);
	}

	@Override
	public <U> CompletableFuture<U> thenCompose(
			Function<? super T, ? extends CompletionStage<U>> fn)
	{
		return cF.thenCompose(fn);
	}

	@Override
	public <U> CompletableFuture<U> thenComposeAsync(
			Function<? super T, ? extends CompletionStage<U>> fn)
	{
		return cF.thenComposeAsync(fn);
	}

	@Override
	public <U> CompletableFuture<U> thenComposeAsync(
			Function<? super T, ? extends CompletionStage<U>> fn, Executor executor)
	{
		return cF.thenComposeAsync(fn, executor);
	}

	@Override
	public CompletableFuture<T> whenComplete(BiConsumer<? super T, ? super Throwable> action)
	{
		return cF.whenComplete(action);
	}

	@Override
	public CompletableFuture<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action)
	{
		return cF.whenCompleteAsync(action);
	}

	@Override
	public CompletableFuture<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action,
			Executor executor)
	{
		return cF.whenCompleteAsync(action, executor);
	}

	@Override
	public <U> CompletableFuture<U> handle(BiFunction<? super T, Throwable, ? extends U> fn)
	{
		return cF.handle(fn);
	}

	@Override
	public <U> CompletableFuture<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn)
	{
		return cF.handleAsync(fn);
	}

	@Override
	public <U> CompletableFuture<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn,
			Executor executor)
	{
		return cF.handleAsync(fn, executor);
	}

	@Override
	public CompletableFuture<T> toCompletableFuture()
	{
		return cF.toCompletableFuture();
	}

	@Override
	public CompletableFuture<T> exceptionally(Function<Throwable, ? extends T> fn)
	{
		return cF.exceptionally(fn);
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning)
	{
		return cF.cancel(mayInterruptIfRunning);
	}

	@Override
	public boolean isCancelled()
	{
		return cF.isCancelled();
	}

	@Override
	public String toString()
	{
		return cF.toString();
	}

}
