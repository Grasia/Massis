package com.massisframework.massis3.simulation.server.eventbus.services;

import java.util.concurrent.CompletionStage;

import io.vertx.core.Future;

public class CompletionFutures {

	public static <T> Future<T> fromCompletionStage(CompletionStage<T> completionStage)
	{
		Future<T> f = Future.future();
		completionStage.whenComplete((res, ex) -> {
			if (ex != null)
			{
				f.fail(ex);
			} else
			{
				f.complete(res);
			}
		});
		return f;
	}

}
