package com.massisframework.massis3.core.systems.control.human;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.simsilica.es.EntityId;

public class HumanCheckException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6267382780508953458L;

	private HumanCheckException(EntityId id)
	{
		this(id.getId());
	}

	private HumanCheckException(long id)
	{
		this("The id provided [" + id + "] does not belong to any human entity");
	}

	private HumanCheckException(String msg)
	{
		super(msg);
	}

	public HumanCheckException(Throwable cause)
	{
		super(cause);
	}

	public HumanCheckException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public HumanCheckException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public static HumanCheckException create(long id)
	{
		return new HumanCheckException(id);
	}

	public static HumanCheckException create(EntityId id)
	{
		return new HumanCheckException(id);
	}

	public static <T> CompletionStage<T> createAsStage(long id)
	{
		CompletableFuture<T> cF = new CompletableFuture<T>();
		HumanCheckException ex = new HumanCheckException(id);
		cF.completeExceptionally(ex);
		return cF;
	}

	public static <T> CompletionStage<T> createAsStage(String msg)
	{
		CompletableFuture<T> cF = new CompletableFuture<T>();
		HumanCheckException ex = new HumanCheckException(msg);
		cF.completeExceptionally(ex);
		return cF;
	}

	public static CompletionStage<EntityId> createAsStage(Exception e)
	{
		CompletableFuture<EntityId> cF = new CompletableFuture<>();
		HumanCheckException ex = new HumanCheckException(e);
		cF.completeExceptionally(ex);
		return cF;
	}

}
