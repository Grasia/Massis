package com.massisframework.massis3.web.response.impl;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.streams.ReadStream;

public class LocalMapReadStream<K, V> implements ReadStream<V> {

	private static final Logger log = LoggerFactory.getLogger(LocalMapReadStream.class);
	private Handler<Throwable> exceptionHandler;
	private Handler<V> handler;
	private AtomicBoolean paused;
	private AtomicLong timerId;
	private long readInterval;
	private Vertx vertx;
	private String localMapName;
	private K localMapKey;

	public LocalMapReadStream(Vertx vertx, long readInterval, String localMapName,
			K localMapKey)
	{
		this.vertx = vertx;
		this.timerId = new AtomicLong(Long.MIN_VALUE);
		this.readInterval = readInterval;
		this.paused = new AtomicBoolean(true);
		this.localMapName = localMapName;
		this.localMapKey = localMapKey;
	}

	public void start()
	{
		this.enableTimer();
	}

	@Override
	public ReadStream<V> exceptionHandler(Handler<Throwable> handler)
	{
		this.exceptionHandler = handler;
		return this;
	}

	@Override
	public ReadStream<V> handler(@Nullable Handler<V> handler)
	{
		synchronized (this)
		{
			if (handler == null)
			{
				this.disableTimer();
			}
			this.handler = handler;
			return this;
		}
	}

	@Override
	public ReadStream<V> pause()
	{
		this.disableTimer();
		return this;
	}

	@Override
	public ReadStream<V> resume()
	{
		this.enableTimer();
		return this;
	}

	@Override
	public ReadStream<V> endHandler(@Nullable Handler<Void> endHandler)
	{
//		this.endHandler = endHandler;
		System.out.println("End handler set");
		return this;
	}

	private void disableTimer()
	{
		if (this.paused.getAndSet(true))
		{
			System.out.println("Disabled read");
			vertx.cancelTimer(this.timerId.get());
		}
	}

	private void enableTimer()
	{
		if (this.paused.getAndSet(false))
		{
			System.out.println("Enabled read");
			vertx.cancelTimer(this.timerId.get());
			this.timerId.set(vertx.setPeriodic(this.readInterval, tId -> {
				if (paused.get())
				{
					vertx.cancelTimer(tId);
					return;
				}
				V data = vertx.sharedData().<K, V> getLocalMap(localMapName).get(this.localMapKey);
				if (data != null && this.handler != null)
				{
					this.handler.handle(data);
				}
			}));
		}
	}
}
