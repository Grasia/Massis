package com.massisframework.massis3.services.sockjs.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageProducer;

public class CloseableMessageProducer<T> implements MessageProducer<T> {

	private Handler<Void> closeHandler;
	private MessageProducer<T> delegate;

	public CloseableMessageProducer(MessageProducer<T> delegate)
	{
		this.delegate = delegate;
	}

	public MessageProducer<T> send(T message)
	{
		return delegate.send(message);
	}

	public <R> MessageProducer<T> send(T message, Handler<AsyncResult<Message<R>>> replyHandler)
	{
		return delegate.send(message, replyHandler);
	}

	public MessageProducer<T> exceptionHandler(Handler<Throwable> handler)
	{
		return delegate.exceptionHandler(handler);
	}

	public MessageProducer<T> write(T data)
	{
		return delegate.write(data);
	}

	public MessageProducer<T> setWriteQueueMaxSize(int maxSize)
	{
		return delegate.setWriteQueueMaxSize(maxSize);
	}

	public MessageProducer<T> drainHandler(Handler<Void> handler)
	{
		return delegate.drainHandler(handler);
	}

	public MessageProducer<T> deliveryOptions(DeliveryOptions options)
	{
		return delegate.deliveryOptions(options);
	}

	public String address()
	{
		return delegate.address();
	}

	public void end()
	{
		delegate.end();
	}

	public void end(T t)
	{
		delegate.end(t);
	}

	public void close()
	{
		delegate.close();
		synchronized (delegate)
		{
			if (this.closeHandler != null)
			{
				this.closeHandler.handle(null);
			}
		}
	}

	public void closeHandler(Handler<Void> handler)
	{
		synchronized (delegate)
		{
			this.closeHandler = handler;
		}
	}

	public boolean writeQueueFull()
	{
		return delegate.writeQueueFull();
	}

}
