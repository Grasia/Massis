package com.massisframework.massis3.commons.app.server.impl;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import com.jme3.app.SimpleApplication;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.post.SceneProcessor;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Spatial;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image;
import com.jme3.util.BufferUtils;
import com.massisframework.massis3.commons.app.cam.CameraUtils;
import com.massisframework.massis3.commons.app.server.InternalServerCamera;

public abstract class AbstractServerCamera implements InternalServerCamera, SceneProcessor {

	protected ViewPort vp;
	protected Camera cam;

	protected AtomicBoolean locationChanged;
	protected AtomicBoolean rotationChanged;
	protected ServerFlyByCamera flyByCam;

	protected SimpleApplication app;

	protected Vector3f location;
	protected Quaternion rotation;

	protected boolean isMain;
	protected String id;

	protected final ByteBuffer renderCPUBuff;
	protected BufferedImage renderImage;
	protected int colorComponents = 4;

	protected AtomicBoolean released;

	private byte[] camImageRawData;

	public AbstractServerCamera(ViewPort vp, SimpleApplication app)
	{
		this.app = app;
		this.cam = vp.getCamera();

		this.id = UUID.randomUUID().toString();

		this.vp = vp;

		this.locationChanged = new AtomicBoolean(false);
		this.rotationChanged = new AtomicBoolean(false);

		this.location = this.cam.getLocation().clone();
		this.rotation = this.cam.getRotation().clone();

		this.flyByCam = new ServerFlyByCamera(this);
		this.flyByCam.setMoveSpeed(80f);
		this.released = new AtomicBoolean(false);

		this.renderCPUBuff = BufferUtils
				.createByteBuffer(cam.getWidth() * cam.getHeight() * colorComponents);

		this.renderImage = createRenderImage();
		this.vp.addProcessor(this);

	}

	private BufferedImage createRenderImage()
	{
		return new BufferedImage(cam.getWidth(), cam.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
	}

	@Override
	public String getId()
	{
		return id;
	}

	public void setMainCam(boolean main)
	{
		if (this.isMain == main)
		{
			return;
		}

		if (!this.isMain && main)
		{
			this.app.enqueue(() -> {
				this.flyByCam.registerWithInput(app.getInputManager());
			});
		} else if (this.isMain && !main)
		{
			this.app.enqueue(() -> {
				this.flyByCam.unregisterInput();
			});
		}

		this.isMain = main;
	}

	@Override
	public BufferedImage getImage()
	{
		return this.renderImage;
	}

	protected void update()
	{
		if (this.locationChanged.getAndSet(false))
		{
			this.cam.setLocation(location);
		}
		if (this.rotationChanged.getAndSet(false))
		{
			this.cam.setRotation(rotation);
		}
	}

	@Override
	public void attachScene(Spatial scene)
	{
		try
		{
			this.vp.attachScene(scene);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}

	}

	@Override
	public void detachScene(Spatial scene)
	{
		try
		{
			app.enqueue(() -> {
				this.vp.detachScene(scene);
				return null;
			}).get();
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public boolean hasViewPort(ViewPort vp)
	{
		return this.vp == vp;
	}

	@Override
	public void setLocation(Vector3f loc)
	{
		this.locationChanged.set(true);
		this.location.set(loc);
	}

	@Override
	public void setRotation(Quaternion rot)
	{
		this.rotationChanged.set(true);
		this.rotation.set(rot);
	}

	protected Camera getCam()
	{
		return cam;
	}

	public float getFrustumBottom()
	{
		return cam.getFrustumBottom();
	}

	public float getFrustumFar()
	{
		return cam.getFrustumFar();
	}

	public float getFrustumLeft()
	{
		return cam.getFrustumLeft();
	}

	public float getFrustumNear()
	{
		return cam.getFrustumNear();
	}

	public float getFrustumRight()
	{
		return cam.getFrustumRight();
	}

	public float getFrustumTop()
	{
		return cam.getFrustumTop();
	}

	@Override
	public Vector3f getDirection()
	{
		return cam.getDirection();
	}

	public Vector3f getLeft()
	{
		return cam.getLeft();
	}

	public Vector3f getUp()
	{
		return cam.getUp();
	}

	@Override
	public Vector3f getDirection(Vector3f store)
	{
		return cam.getDirection(store);
	}

	public Vector3f getLeft(Vector3f store)
	{
		return cam.getLeft(store);
	}

	public Vector3f getUp(Vector3f store)
	{
		return cam.getUp(store);
	}

	@Override
	public int getHeight()
	{
		return cam.getHeight();

	}

	@Override
	public Vector3f getLocation()
	{
		return this.location;
	}

	@Override
	public Quaternion getRotation()
	{
		return this.rotation;
	}

	public void setCamFrustumTop(float h)
	{
		this.app.enqueue(() -> cam.setFrustumTop(h));
	}

	public void setCamFrustumBottom(float h)
	{
		this.app.enqueue(() -> cam.setFrustumBottom(h));
	}

	public void setCamFrustumLeft(float h)
	{
		this.app.enqueue(() -> cam.setFrustumLeft(h));
	}

	public void setCamFrustumRight(float h)
	{
		this.app.enqueue(() -> cam.setFrustumRight(h));
	}

	public void setAxes(Quaternion axes)
	{
		this.setRotation(axes);
	}

	@Override
	public void initialize(RenderManager rm, ViewPort vp)
	{

	}

	@Override
	public void reshape(ViewPort vp, int w, int h)
	{
		this.renderImage = createRenderImage();
	}

	@Override
	public boolean isInitialized()
	{
		return true;
	}

	@Override
	public void preFrame(float tpf)
	{
		update();
	}

	@Override
	public void postQueue(RenderQueue rq)
	{

	}

	@Override
	public void cleanup()
	{

	}

	@Override
	public void postFrame(FrameBuffer ofb)
	{

		int width = vp.getCamera().getWidth();
		int height = vp.getCamera().getHeight();
		int buffLength = width * height * 3;

		ByteBuffer outBuf = getSharedBuffer(buffLength);
		app.getRenderManager().getRenderer().readFrameBufferWithFormat(ofb, outBuf,
				Image.Format.BGR8);

		if (camImageRawData == null || camImageRawData.length != buffLength)
		{
			camImageRawData = new byte[buffLength];
		}
		outBuf.get(camImageRawData);
		CameraUtils.flip_Y_BGR(camImageRawData, width, height);
		processFrame(camImageRawData, width, height);
	}

	// https://stackoverflow.com/a/12062505/3315914
	private void processFrame(byte[] frame, int width, int height)
	{
		if (this.renderImage == null
				|| this.renderImage.getWidth() != width
				|| this.renderImage.getHeight() != height)
		{
			this.renderImage = this.createRenderImage();
		}
		byte[] imgData = ((DataBufferByte) renderImage.getRaster().getDataBuffer()).getData();
		System.arraycopy(frame, 0, imgData, 0, frame.length);
	}

	@Override
	public boolean isReleased()
	{
		return this.released.get();
	}

	@Override
	public void release()
	{
		// TODO maybe include Releasing state
		if (released.getAndSet(true))
		{
			this.internalRelease();
		}

	}

	protected abstract void internalRelease();

	// TODO duplicated code from ImageRenderAppState

	private static ByteBuffer getSharedBuffer(int size)
	{
		SharedCPUByteBuffer current = SharedCPUByteBuffer.cpuByteBufferTL.get();
		if (current.byteBuffer != null)
		{
			current.byteBuffer.clear();
		}
		current.byteBuffer = BufferUtils.ensureLargeEnough(current.byteBuffer, size);
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

}
