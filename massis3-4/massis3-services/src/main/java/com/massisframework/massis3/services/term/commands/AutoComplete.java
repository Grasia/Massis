package com.massisframework.massis3.services.term.commands;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.List;

import io.vertx.core.Handler;
import io.vertx.ext.shell.cli.Completion;

@Retention(RUNTIME)
@Target(METHOD)
public @interface AutoComplete {

	public Class<? extends AutoCompleteCallback> value();

	public static interface AutoCompleteCallback {
		public void complete(String value, Completion completion, Handler<List<String>> handler);
	}

	public static class NoOPCallback implements AutoCompleteCallback {

		@Override
		public void complete(String value, Completion completion, Handler<List<String>> handler)
		{
			handler.handle(Collections.emptyList());
		}
	}
}
