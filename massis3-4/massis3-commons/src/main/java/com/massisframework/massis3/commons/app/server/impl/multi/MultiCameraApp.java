package com.massisframework.massis3.commons.app.server.impl.multi;

import static com.massisframework.massis3.commons.app.cam.CameraUtils.newCamera;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.audio.AudioContext;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.profile.AppStep;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.shape.Quad;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext.Type;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.massisframework.massis3.commons.app.lwjgl.LwjglMultiWindow;
import com.massisframework.massis3.commons.app.server.InternalServerCamera;
import com.massisframework.massis3.commons.app.server.ServerCamera;
import com.massisframework.massis3.commons.app.server.impl.ServerDebugKeysAppState;
import com.massisframework.massis3.commons.spatials.Materials;
import com.massisframework.massis3.commons.spatials.Spatials;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public class MultiCameraApp extends SimpleApplication {

	private static final Logger log = LoggerFactory.getLogger(MultiCameraApp.class);
	private Node camerasNode;
	private InternalServerCamera mainCam;
	protected CopyOnWriteArrayList<InternalServerCamera> cameras = new CopyOnWriteArrayList<>();
	private Future<MultiCameraApp> startFuture;
	private static AtomicLong APP_COUNTER = new AtomicLong();

	public static synchronized void launch(boolean windowed,
			Handler<AsyncResult<MultiCameraApp>> handler)
	{
		launch(windowed, null, handler);
	}

	public static void launch(boolean windowed, String name,
			Handler<AsyncResult<MultiCameraApp>> handler)
	{
		MultiCameraApp app = new MultiCameraApp(handler);
		app.setSettings(defaultAppSettings());
		app.startInNewThread(windowed, name);
	}

	private MultiCameraApp(Handler<AsyncResult<MultiCameraApp>> handler)
	{
		super((AppState[]) null);

		this.startFuture = Future.future();
		this.startFuture.setHandler(handler);
		this.camerasNode = new Node();
	}

	private void printAppStates()
	{
		try
		{
			Method m = AppStateManager.class.getDeclaredMethod("getStates");
			m.setAccessible(true);
			AppState[] states = (AppState[]) m.invoke(stateManager);
			System.out.print(Arrays.toString(states));
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void startInNewThread(final boolean windowed, String name)
	{
		APP_COUNTER.incrementAndGet();
		Thread t = new Thread(() -> {
			try
			{
				if (windowed)
				{
					this.start(Type.Display, false);
				} else
				{
					this.start(Type.OffscreenSurface, false);
				}
			} catch (Exception ex)
			{
				if (!startFuture.isComplete())
				{
					startFuture.fail(ex);
				} else
				{
					log.error("Error in JME3 App", ex);
				}
			}
		});
		if (name == null)
		{
			name = "<no name>";
		}
		t.setName("[MultiApp#" + APP_COUNTER.get() + "](" + name + ")");
		t.start();
	}

	boolean restarted = false;

	@Override
	public void simpleInitApp()
	{
		this.stateManager.attachAll(initialStates());
		this.setPauseOnLostFocus(false);
		this.inputManager.setCursorVisible(true);
		this.getGuiNode().attachChild(camerasNode);
		this.configureCamListener();

		this.startFuture.complete(this);

		printAppStates();
	}

	public InternalServerCamera getMainCamera()
	{
		return this.mainCam;
	}

	private AppState[] initialStates()
	{
		return new AppState[] {
				new StatsAppState(),
				new ServerDebugKeysAppState(this::getMainCamera)
		};
	}

	public void createCamera(int width, int height,
			Handler<AsyncResult<InternalServerCamera>> handler)
	{
		this.enqueue(() -> {
			try
			{
				InternalServerCamera c = this.createCameraInAppThread(width, height);
				handler.handle(Future.succeededFuture(c));
			} catch (Exception e)
			{
				handler.handle(Future.failedFuture(e));
			}
		});
	}

	public void removeCamera(ServerCamera cam, Handler<AsyncResult<Void>> handler)
	{
		if (this.cameras.remove(cam))
		{
			this.enqueue(() -> {
				try
				{
					((InternalServerCamera) cam).release();
					handler.handle(Future.succeededFuture());
				} catch (Exception e)
				{
					handler.handle(Future.failedFuture(e));
				}
			});

		}
	}

	Geometry box = Spatials.createBox(new Vector3f(), Materials.newUnshaded(ColorRGBA.Red));

	private InternalServerCamera createCameraInAppThread(int width, int height)
	{

		Camera offCamera = newCamera(width, height);

		ViewPort offView = this.getRenderManager()
				.createPreView("View_" + UUID.randomUUID().toString(), offCamera);

		
		offView.attachScene(box);
		offView.setClearFlags(true, true, true);

		offView.setBackgroundColor(ColorRGBA.Black);

		// create offscreen framebuffer
		FrameBuffer offBuffer = new FrameBuffer(width, height, 1);

		// setup framebuffer's cam
		// offCamera.setFrustumPerspective(45f, 1f, 1f, 1000f);
		offCamera.setLocation(new Vector3f(0f, 0f, -50));
		offCamera.lookAt(new Vector3f(0f, 0f, 0f), Vector3f.UNIT_Y);

		// setup framebuffer's texture
		Texture2D offTex = new Texture2D(width, height, Image.Format.RGBA8);
		offTex.setMinFilter(Texture.MinFilter.Trilinear);
		offTex.setMagFilter(Texture.MagFilter.Bilinear);

		// setup framebuffer to use texture
		offBuffer.setDepthBuffer(Format.Depth);
		offBuffer.setColorTexture(offTex);

		// set viewport to render to offscreen framebuffer
		offView.setOutputFrameBuffer(offBuffer);

		InternalServerCamera serverCamera = new MultiApplicationCameraImpl(offView, this);

		this.cameras.add(serverCamera);

		Geometry quad = new Geometry("box", new Quad(width, height));
		quad.setName(serverCamera.getId());
		quad.setCullHint(CullHint.Never);

		Material mat = Materials.newUnshaded();
		mat.setTexture("ColorMap", offTex);
		quad.setMaterial(mat);

		this.guiNode.attachChildAt(quad, 0);

		if (cameras.size() == 1)
		{
			// quad.setCullHint(CullHint.Inherit);
			serverCamera.setMainCam(true);
			this.mainCam = serverCamera;
		}
		else {
			serverCamera.setMainCam(false);
		}
		offView.setEnabled(true);
		return serverCamera;

	}

	private void configureCamListener()
	{
		String mapping = UUID.randomUUID().toString();
		AtomicInteger currentCam = new AtomicInteger(0);
		this.getInputManager().addMapping(mapping, new KeyTrigger(KeyInput.KEY_RETURN));
		this.getInputManager().addListener((ActionListener) (name, pressed, tpf) -> {
			if (pressed)
			{
				if (this.cameras.isEmpty())
				{
					return;
				}
				currentCam.incrementAndGet();
				if (currentCam.get() >= this.cameras.size())
				{
					currentCam.set(0);
				}
				this.cameras.forEach(s -> s.setMainCam(false));
				this.camerasNode.getChildren().forEach(c -> {
					if (c.getName().equals(this.cameras.get(currentCam.get()).getId()))
					{
						// c.setCullHint(CullHint.Inherit);
					} else
					{
						// c.setCullHint(CullHint.Always);
					}
				});
				this.mainCam = this.cameras.get(currentCam.get());
				this.mainCam.setMainCam(true);
			}
		}, mapping);
	}

	private static AppSettings defaultAppSettings()
	{
		AppSettings settings = new AppSettings(true);
		settings.setCustomRenderer(LwjglMultiWindow.class);
		settings.setAudioRenderer(null);
		settings.setTitle("MASSIS 3 Simulator");
		return settings;
	}

	public List<ServerCamera> getCameras()
	{
		return Collections.unmodifiableList(this.cameras);
	}

	/////////////////////////

	@Override
	public void simpleUpdate(float tpf)
	{
		box.updateGeometricState();
	}

}
