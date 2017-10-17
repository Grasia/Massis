package com.massisframework.massis3.services.sockjs.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.streams.ReadStream;

public class CloseableMessageConsumer<T> implements MessageConsumer<T> {

	private Handler<Void> closeHandler;
	private MessageConsumer<T> delegate;

	public CloseableMessageConsumer(MessageConsumer<T> delegate)
	{
		this.delegate = delegate;
	}

	public MessageConsumer<T> exceptionHandler(Handler<Throwable> handler)
	{
		return delegate.exceptionHandler(handler);
	}

	public MessageConsumer<T> handler(Handler<Message<T>> handler)
	{
		return delegate.handler(handler);
	}

	public MessageConsumer<T> pause()
	{
		return delegate.pause();
	}

	public MessageConsumer<T> resume()
	{
		return delegate.resume();
	}

	public MessageConsumer<T> endHandler(Handler<Void> endHandler)
	{
		return delegate.endHandler(endHandler);
	}

	public ReadStream<T> bodyStream()
	{
		return delegate.bodyStream();
	}

	public boolean isRegistered()
	{
		return delegate.isRegistered();
	}

	public String address()
	{
		return delegate.address();
	}

	public MessageConsumer<T> setMaxBufferedMessages(int maxBufferedMessages)
	{
		return delegate.setMaxBufferedMessages(maxBufferedMessages);
	}

	public int getMaxBufferedMessages()
	{
		return delegate.getMaxBufferedMessages();
	}

	public void completionHandler(Handler<AsyncResult<Void>> completionHandler)
	{
		delegate.completionHandler(completionHandler);
	}

	public void unregister()
	{
		this.unregister(r -> {
		});
	}

	public void unregister(Handler<AsyncResult<Void>> completionHandler)
	{
		delegate.unregister(r -> {
			synchronized (delegate)
			{
				completionHandler.handle(r);
				if (this.closeHandler != null)
				{
					this.closeHandler.handle(null);
				}
			}
		});
	}

}
