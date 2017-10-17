package com.massisframework.massis3.web.response;

import com.massisframework.massis3.web.response.impl.MJPEGStreamerImpl;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.web.RoutingContext;

public interface MJPEGStreamer extends WriteStream<Buffer> {

	void stream();

	public static MJPEGStreamer create(RoutingContext ctx, ReadStream<Buffer> imageReadStream)
	{
		return new MJPEGStreamerImpl(ctx, imageReadStream);
	}
	
	
}