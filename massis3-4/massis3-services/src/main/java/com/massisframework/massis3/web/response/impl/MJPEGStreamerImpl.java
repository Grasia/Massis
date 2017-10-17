package com.massisframework.massis3.web.response.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.massisframework.massis3.web.response.MJPEGStreamer;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.Pump;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.shell.term.impl.Helper;
import io.vertx.ext.web.RoutingContext;

/**
 * Generates an HTTP MJPEG stream from a supplier
 * 
 * @author rpax
 *
 */
public class MJPEGStreamerImpl implements MJPEGStreamer {

	private static final Logger log = LoggerFactory.getLogger(MJPEGStreamerImpl.class);
	public static final Buffer NOT_AVAILABLE_IMAGE = notFoundImage();
	private static final String BOUNDARY_STRING = "--Boundary";
	private ReadStream<Buffer> imageSupplier;
	private RoutingContext rctx;
	private Pump pump;

	public MJPEGStreamerImpl(RoutingContext rctx, ReadStream<Buffer> imageSupplier)
	{
		this.imageSupplier = imageSupplier;
		this.rctx = rctx;
		this.configureHeaders();
	}

	private void configureHeaders()
	{
		/**
		 * @formatter:off
		 */
		rctx.response().setChunked(true);
		rctx.response().putHeader("Accept-Range", "bytes");
		rctx.response().putHeader("Max-Age", "0");
		rctx.response().putHeader("Expires", "0");
		rctx.response().putHeader("Connection", "close");
		rctx.response().putHeader("Cache-Control","no-store, private, no-cache, must-revalidate, pre-check=0, post-check=0,max-age=0");
		rctx.response().putHeader("Content-Type", "multipart/x-mixed-replace;boundary=" + BOUNDARY_STRING);
		rctx.response().putHeader("Pragma", "no-cache");
		/**
		 * @formatter:on
		 */
		rctx.response().write(Buffer.buffer(BOUNDARY_STRING + "\r\n"));
		
	}

	private static Buffer notFoundImage()
	{
		return Helper.loadResource("/web/datanotavailable.jpg");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.massisframework.massis3.services.http.response.impl.MJPEGStreamer#
	 * stream()
	 */
	@Override
	public void stream()
	{
		int writeFullQueueSize = 256;
		this.rctx.response().setWriteQueueMaxSize(writeFullQueueSize);
		this.pump = Pump.pump(this.imageSupplier, this, writeFullQueueSize);
		this.rctx.response().endHandler(_void -> {
			System.out.println("Ended response");
			pump.stop();
		});
		this.pump.start();

	}

	private Buffer transform(Buffer image)
	{
		if (image == null)
		{
			image = NOT_AVAILABLE_IMAGE;
		}
		Buffer buff = Buffer.buffer(
				//
				"Content-type: image/jpeg\r\n" +
						"Content-Length: " + image.length() + "\r\n" +
						"\r\n")
				.appendBuffer(image)
				.appendBuffer(Buffer.buffer(BOUNDARY_STRING + "\r\n"));
		return buff;
	}

	@Override
	public WriteStream<Buffer> exceptionHandler(Handler<Throwable> handler)
	{
		this.rctx.response().exceptionHandler(handler);
		return this;
	}

	@Override
	public WriteStream<Buffer> write(Buffer image)
	{
		if (!this.rctx.response().ended())
		{
			this.rctx.response().write(transform(image));
		}
		return this;
	}

	@Override
	public void end()
	{
		this.rctx.response().end();
	}

	@Override
	public WriteStream<Buffer> setWriteQueueMaxSize(int maxSize)
	{
		this.rctx.response().setWriteQueueMaxSize(maxSize);
		return this;
	}

	@Override
	public boolean writeQueueFull()
	{
		return this.rctx.response().writeQueueFull();
	}

	@Override
	public WriteStream<Buffer> drainHandler(@Nullable Handler<Void> handler)
	{
		this.rctx.response().drainHandler(handler);
		return this;
	}

}
