package com.massisframework.massis3.core.systems.camera.imagewriter;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import com.jme3.app.state.AbstractAppState;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image;
import com.jme3.util.BufferUtils;
import com.massisframework.massis3.commons.app.cam.CameraUtils;

public class ImageRenderAppState extends AbstractAppState {

	private CopyOnWriteArrayList<RenderCamListener> cameraListeners;
	private RenderManager rm;
	private Map<String, byte[]> vpData;

	public ImageRenderAppState()
	{
		this.cameraListeners = new CopyOnWriteArrayList<>();
		this.vpData = new HashMap<>();
	}

	@Override
	public void cleanup()
	{

	}

	public boolean isReady()
	{
		return this.rm != null;
	}

	public void addListener(String viewportName, RenderImageListener listener)
	{
		Objects.requireNonNull(viewportName);
		this.cameraListeners.add(new RenderCamListener(listener, viewportName));
	}

	public void removeListener(RenderImageListener listener)
	{
		Objects.requireNonNull(listener);
		this.cameraListeners.removeIf(rcl -> rcl.listener == listener);
	}

	public void removeListener(String viewportName, RenderImageListener listener)
	{
		Objects.requireNonNull(listener);
		Objects.requireNonNull(viewportName);
		this.cameraListeners
				.removeIf(rcl -> rcl.listener == listener && rcl.vpName.equals(viewportName));
	}

	public int getViewportWidth(String viewportName)
	{
		if (!this.isReady())
			throw new IllegalStateException("Render state not ready");
		return CameraUtils.getViewPortByName(rm, viewportName).getCamera().getWidth();
	}

	public int getViewportHeight(String viewportName)
	{
		if (!this.isReady())
			throw new IllegalStateException("Render state not ready");
		return CameraUtils.getViewPortByName(this.rm, viewportName).getCamera().getHeight();
	}

	public byte[] getCameraImageData(String vpName, byte[] store)
	{
		synchronized (this)
		{
			if (!this.isReady())
				throw new IllegalStateException("Render state not ready");
			byte[] cdata = this.vpData.get(vpName);
			if (store == null || store.length != cdata.length)
				store = new byte[cdata.length];
			System.arraycopy(cdata, 0, store, 0, cdata.length);
		}
		return store;
	}

	@Override
	public void render(RenderManager rm)
	{
		this.rm = rm;
		CameraUtils.forEachViewPort(this.rm, vp -> {
			synchronized (this)
			{
				if (vp == null || !areListenersFor(vp))
					return;
				// System.out.println(vp.getName());
				FrameBuffer ofb = vp.getOutputFrameBuffer();

				int width = vp.getCamera().getWidth();
				int height = vp.getCamera().getHeight();
				int buffLength = width * height * 3;
				ByteBuffer outBuf = getSharedBuffer(buffLength);
				rm.getRenderer().readFrameBufferWithFormat(ofb, outBuf, Image.Format.RGB8);
				byte[] camImage = this.vpData.get(vp.getName());
				if (camImage == null || camImage.length != buffLength)
				{
					camImage = new byte[buffLength];
					this.vpData.put(vp.getName(), camImage);
				}
				outBuf.get(camImage);
				CameraUtils.flip_Y_RGB(camImage, width, height);
				for (RenderCamListener rcl : cameraListeners)
				{
					// System.out.println(rcl);
					if (rcl.matchesVP(vp))
					{
						rcl.listener.renderReady(vp.getName());
					}
				}
			}
		});
	}

	private boolean areListenersFor(ViewPort vp)
	{
		for (RenderCamListener rcl : cameraListeners)
		{
			if (rcl.matchesVP(vp) && rcl.listener.requiresNewFrame(vp.getName()))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public void postRender()
	{

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

	private static class RenderCamListener {
		private RenderImageListener listener;
		private String vpName;

		public RenderCamListener(RenderImageListener listener, String vpName)
		{
			this.listener = listener;
			this.vpName = vpName;
		}

		private boolean matchesVP(ViewPort vp)
		{
			return this.vpName.equals(vp.getName());
		}
	}
}
