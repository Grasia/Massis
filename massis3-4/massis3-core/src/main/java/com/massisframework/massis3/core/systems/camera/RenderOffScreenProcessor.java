package com.massisframework.massis3.core.systems.camera;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.jme3.post.FilterPostProcessor;
import com.jme3.post.SceneProcessor;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.texture.FrameBuffer;
import com.jme3.util.BufferUtils;

public class RenderOffScreenProcessor implements SceneProcessor {

	private RenderManager renderManager;
	private ViewPort viewPort;
	private int width;
	private int height;
	private VideoSequenceWriter videoWriter;
	private String output;
	private boolean initialized;
	private AtomicBoolean writing;

	private FrameBuffer originalFB;

	public RenderOffScreenProcessor(String output)
	{
		this.output = output;
		this.initialized = false;
	}

	@Override
	public void initialize(RenderManager rm, ViewPort vp)
	{
		this.renderManager = rm;
		this.width = vp.getCamera().getWidth();
		this.height = vp.getCamera().getHeight();
		this.viewPort = vp;
		// this.originalFB=vp.getOutputFrameBuffer();
		this.writing = new AtomicBoolean(true);
		this.videoWriter = new JavaCVWriter(width, height);
		videoWriter.start(output);
		Runtime.getRuntime().addShutdownHook(new Thread(this::cleanup));
		this.reshape(vp, vp.getCamera().getWidth(), vp.getCamera().getHeight());
		this.initialized = true;

	}

	@Override
	public void reshape(ViewPort vp, int w, int h)
	{
		this.width = w;
		this.height = h;
		this.videoWriter.setWidth(width);
		this.videoWriter.setHeight(height);
	}

	@Override
	public boolean isInitialized()
	{
		return this.initialized;
	}

	private float lastTPF = 0;

	@Override
	public void preFrame(float tpf)
	{
		this.lastTPF = tpf;
	}

	@Override
	public void postQueue(RenderQueue rq)
	{

	}

	int frame_count = 0;

	@Override
	public void postFrame(FrameBuffer out)
	{
		if (!this.initialized)
		{
			return;
		}
		if (!this.isFirstProcessor())
		{
			return;
		}
		//
		frame_count++;
		this.countTime(lastTPF, frame_count);
		final Renderer renderer = this.renderManager.getRenderer();

		ByteBuffer cpuBuff = getSharedBuffer(this.width * this.height * 4);

		renderer.readFrameBuffer(null, cpuBuff);
		try
		{
			this.videoWriter.writePicture(cpuBuff);
		} catch (RuntimeException e)
		{
			System.err.println(
					"Problematic viewPort: " + this.viewPort.getName());
			throw e;
		}
	}

	public byte[] getLastImage()
	{
		return this.videoWriter.getLastImage();
	}

	static long ONE_SECOND_NANO = 1000 * 1000 * 1000;
	static long TIME_SLOT = ONE_SECOND_NANO * 30;
	float tpfTime = -1;
	long deltaTime = 0;
	long lastFrame = -1;
	long slotTime = 0;
	long startTime = 0;
	private long startMS;

	private void countTime(float tpf, int frameCount)
	{
		long currentFrame = System.nanoTime();
		if (lastFrame == -1)
		{
			deltaTime = 0;
			startTime = currentFrame;
			lastFrame = currentFrame;
			tpfTime = 0;
			startMS = System.currentTimeMillis();
			return;
		} else
		{

			deltaTime = currentFrame - lastFrame;
			tpfTime += tpf;
			lastFrame = currentFrame;
		}

		float realTimePassedInSeconds = (float) ((1D
				* (currentFrame - startTime)) / ONE_SECOND_NANO);

		if (slotTime > TIME_SLOT)
		{
			slotTime = 0;
			if (this.viewPort != null)
				System.err.println("VP:[" + this.viewPort.getName() + "]\n"
						+ "time: "
						+ tpfTime + ".MS: "
						+ (System.currentTimeMillis() - startMS) / 1000
						+ ". Real: "
						+ realTimePassedInSeconds + "("
						+ realTimePassedInSeconds / 60f + "). Rate: "
						+ tpfTime / realTimePassedInSeconds + "\n"
						+ "frameCount " + frameCount);
		} else
		{
			slotTime += deltaTime;
		}
	}

	public boolean isFirstProcessor()
	{
		final List<SceneProcessor> processors = this.viewPort.getProcessors();
		return processors.get(processors.size() - 1) == this;
	}

	@Override
	public void cleanup()
	{
		if (this.writing.getAndSet(false))
		{
			if (this.videoWriter != null)
			{
				this.videoWriter.stop();
			}
		}
	}

	private boolean recovered = false;

	// TODO paso. Hay que darle muchas vueltas.
	private void recoverOriginalFB()
	{
		if (!this.recovered)
		{
			for (SceneProcessor sp : this.viewPort.getProcessors())
			{
				if (sp instanceof FilterPostProcessor)
				{
					FilterPostProcessor fpp = (FilterPostProcessor) sp;
					// Ver
				}
			}
		}
	}

	private static ByteBuffer getSharedBuffer(int size)
	{
		SharedCPUByteBuffer current = SharedCPUByteBuffer.cpuByteBufferTL.get();
		if (current.byteBuffer != null)
		{
			current.byteBuffer.clear();
		}
		current.byteBuffer = BufferUtils.ensureLargeEnough(current.byteBuffer,
				size);
		return current.byteBuffer;
	}

	private static class SharedCPUByteBuffer {
		private ByteBuffer byteBuffer;
		private static ThreadLocal<SharedCPUByteBuffer> cpuByteBufferTL;
		static
		{
			cpuByteBufferTL = ThreadLocal.withInitial(SharedCPUByteBuffer::new);
		}
	}

	public ViewPort getViewPort()
	{
		return this.viewPort;
	}

	public FrameBuffer getOriginalFB()
	{
		return this.originalFB;
	}

}
