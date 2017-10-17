package com.massisframework.massis3.web.response.impl;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.streams.ReadStream;

public class LastEventBusMessageReadStream<V> implements ReadStream<V> {

	private static final Logger log = LoggerFactory.getLogger(LastEventBusMessageReadStream.class);
	private Handler<Throwable> exceptionHandler;
	private Handler<V> handler;
	private AtomicBoolean paused;
	private Vertx vertx;
	private String address;
	private MessageConsumer<V> consumer;

	public static <V> ReadStream<V> create(Vertx vertx, String address)
	{
		return new LastEventBusMessageReadStream<>(vertx, address);
	}

	private LastEventBusMessageReadStream(Vertx vertx, String address)
	{
		this.vertx = vertx;
		this.paused = new AtomicBoolean(true);
		this.address = address;
		this.consumer = this.vertx.eventBus().consumer(address);
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
				this.disableConsumer();
				this.consumer.unregister();
			}
			this.handler = handler;
			return this;
		}
	}

	@Override
	public ReadStream<V> pause()
	{
		this.disableConsumer();
		return this;
	}

	@Override
	public ReadStream<V> resume()
	{
		this.enableConsumer();
		return this;
	}

	@Override
	public ReadStream<V> endHandler(@Nullable Handler<Void> endHandler)
	{
		// this.endHandler = endHandler;
		System.out.println("End handler set");
		return this;
	}

	private void disableConsumer()
	{
		if (this.paused.getAndSet(true))
		{
			// this.consumer.unregister();
		}
	}

	private void onData(Message<V> msg)
	{
		if (this.handler != null && !this.paused.get())
		{
			this.handler.handle(msg.body());
		}
	}

	private void enableConsumer()
	{
		if (this.paused.getAndSet(false))
		{
			if (this.consumer == null || !this.consumer.isRegistered())
			{
				this.consumer = vertx.eventBus().<V> consumer(address).handler(this::onData);
			}
		}
	}
}
