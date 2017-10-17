package com.massisframework.massis3.core.systems.camera;

import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.FrameRecorder.Exception;

public class JavaCVWriter implements VideoSequenceWriter {

	private FrameRecorder recorder;
	private int width;
	private int height;
	private ExecutorService executor;
	private double framerate = 25;
	private BlockingQueue<Frame> toWrite = new ArrayBlockingQueue<>(512);
	private Queue<Frame> cached = new ConcurrentLinkedQueue<>();
	private boolean finished;
	// private byte[] lastImage;
	private int framesRecorded = 0;

	public JavaCVWriter(int width, int height)
	{
		this.width = width;
		this.height = height;
		this.finished = false;
		// this.lastImage=new byte[width*height*4];
	}

	private Logger logger = Logger.getLogger(getClass().getName());
	private int cachedMaxSize = 0;
	private int toWriteMaxSize = 0;

	private void writeLoop()
	{
		try
		{
			try
			{
				while (!this.finished)
				{

					toWriteMaxSize = Math.max(toWriteMaxSize,
							this.toWrite.size());
					cachedMaxSize = Math.max(this.cachedMaxSize,
							this.cached.size());
					//

					Frame f = this.toWrite.take();
					// f.timestamp = (long) ((this.framesRecorded /
					// this.framerate)
					// * 1000 * 1000);
					recorder.setTimestamp(1);
					this.recorder.record(f);
					framesRecorded++;
					this.cached.add(f);
					if (this.framesRecorded % (framerate * 10) == 0)
					{
						logger.info(() -> "Frames Recorded: " + framesRecorded
								+ ". Cached max size: " + this.cachedMaxSize
								+ ". toWriteMaxSize: " + toWriteMaxSize);
					}
				}
			} catch (InterruptedException e)
			{
				this.finished = true;
				while (!this.toWrite.isEmpty())
				{
					this.recorder.record(this.toWrite.poll());
					framesRecorded++;
				}
				logger.info(() -> "Frames Recorded: " + framesRecorded);
				this.recorder.stop();
				this.recorder.release();
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	@Override
	public void start(String output)
	{

		try
		{
			this.recorder = FrameRecorder.createDefault(output, width, height);
			// OJO, esta a pelo
			this.recorder.setFrameRate(this.framerate);
			this.recorder.setVideoQuality(0.9);
			this.executor = Executors.newSingleThreadExecutor();
			try
			{
				this.recorder.start();
			} catch (java.lang.Exception e)
			{
				throw new RuntimeException(e);
			}
			// this.executor.submit(() -> {
			//
			// });
			this.executor.submit(this::writeLoop);
		} catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	private static AtomicLong GLOBAL_TS = new AtomicLong(0);

	@Override
	public void writePicture(ByteBuffer rgbaBuff)
	{

		Frame frame = newFrame();
		frame.timestamp = GLOBAL_TS.getAndIncrement();
		ByteBuffer img = ((ByteBuffer) frame.image[0]);
		img.clear();
		img.put(rgbaBuff);
		// System.arraycopy(img.array(), 0, this.lastImage, 0,
		// this.lastImage.length);
		img.limit(rgbaBuff.limit());
		flipY_fix_rgb(frame);
		try
		{
			this.toWrite.put(frame);// .put(frame);
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}

	}

	@Override
	public byte[] getLastImage()
	{
		// return lastImage;
		return null;
	}

	private static void flipY_fix_rgb(Frame f)
	{
		int height = f.imageHeight;
		int width = f.imageWidth;
		ByteBuffer buf = ((ByteBuffer) f.image[0]);
		for (int y = 0; y < height / 2; y++)
		{
			for (int x = 0; x < width; x++)
			{
				int inPtr0 = (y * width + x) * 3;
				int outPtr0 = ((height - y - 1) * width + x) * 3;
				//

				byte r1 = buf.get(inPtr0 + 0);
				byte g1 = buf.get(inPtr0 + 1);
				byte b1 = buf.get(inPtr0 + 2);

				byte r2 = buf.get(outPtr0 + 0);
				byte g2 = buf.get(outPtr0 + 1);
				byte b2 = buf.get(outPtr0 + 2);

				// cpuArray[outPtr+0] = a1;
				buf.put(outPtr0 + 0, b1);
				buf.put(outPtr0 + 1, g1);
				buf.put(outPtr0 + 2, r1);

				buf.put(inPtr0 + 0, b2);
				buf.put(inPtr0 + 1, g2);
				buf.put(inPtr0 + 2, r2);
			}
		}
	}

	private Frame newFrame()
	{
		Frame f = this.cached.poll();
		if (f == null)
		{
			f = new Frame(width, height, Frame.DEPTH_UBYTE, 3/* RGBA */);
			f.imageChannels = 3;
		}
		return f;
	}

	@Override
	public void stop()
	{

		this.executor.shutdownNow();

	}

	@Override
	public int getWidth()
	{
		return width;
	}

	@Override
	public void setWidth(int width)
	{
		this.width = width;
		this.recorder.setImageWidth(width);
	}

	@Override
	public int getHeight()
	{
		return height;
	}

	@Override
	public void setHeight(int height)
	{
		this.height = height;
		this.recorder.setImageHeight(height);
	}

}
