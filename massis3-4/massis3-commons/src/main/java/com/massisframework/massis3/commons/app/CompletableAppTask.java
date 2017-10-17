package com.massisframework.massis3.commons.app;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;

public interface CompletableAppTask<T> extends Future<T>, CompletionStage<T> {

	public void execute();

	public static <T> CompletableAppTask<T> of(Callable<T> callable)
	{
		return new CompletableAppTaskImpl<>(callable);
	}
}
