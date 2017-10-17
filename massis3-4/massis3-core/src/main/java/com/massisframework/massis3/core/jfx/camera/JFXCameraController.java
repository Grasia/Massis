package com.massisframework.massis3.core.jfx.camera;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.app.state.AppStateManager;
import com.massisframework.massis3.core.jfx.annotations.FXMLPath;
import com.massisframework.massis3.core.jfx.hud.JFXJmeController;
import com.massisframework.massis3.core.systems.camera.imagewriter.ImageRenderAppState;
import com.massisframework.massis3.core.systems.camera.imagewriter.RenderImageListener;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;

@FXMLPath("JFXCameraPane.fxml")
public class JFXCameraController implements JFXJmeController, RenderImageListener {

	private static long RENDER_UPDATE_TIME = 1000L * 1000L * 1000L / 25;
	private long lastRecordTime = 0;
	private static final Logger log = LoggerFactory.getLogger(JFXCameraController.class);
	@FXML
	private ImageView imageView;
	private WritableImage writableImage;
	private PixelWriter pixelWriter;
	private byte[] pixels;
	private String viewportName;
	private ImageRenderAppState imageRender;

	@FXML
	public void initialize()
	{
		// this.imageView.fitWidthProperty()
		// .bind(( (Pane) this.imageView.getParent()).widthProperty());
		// this.imageView.fitHeightProperty()
		// .bind(( (Pane) this.imageView.getParent()).heightProperty());
	}

	private AtomicBoolean needsToRender = new AtomicBoolean(false);
	private long time;

	@Override
	public void jmeInitialize(AppStateManager stateManager)
	{

		if (log.isInfoEnabled())
		{
			log.info("Initializing jme contents of JFXCamera window");
		}
		this.imageRender = stateManager.getState(ImageRenderAppState.class);
		this.attachToCamera("Default");
	}

	public void attachToCamera(String viewPortName)
	{
		synchronized (this.imageView)
		{
			this.viewportName = viewPortName;
			this.imageRender.addListener(this.viewportName, this);
			this.writableImage = new WritableImage(vpHeight(), vpWidth());
			this.pixelWriter = this.writableImage.getPixelWriter();
			this.imageView.setImage(this.writableImage);
			Pane parent = (Pane) this.imageView.getParent();
			this.imageView.fitWidthProperty().bind(parent.widthProperty());
			// this.imageView.fitHeightProperty().bind(parent.heightProperty());
		}
	}

	@Override
	public void renderReady(String vpName)
	{
		needsToRender.set(true);
	}

	@Override
	public void renderFinished()
	{
		if (log.isInfoEnabled())
		{
			log.info("Image renderer finished signal received");
		}
	}

	@Override
	public boolean requiresNewFrame(String viewportName)
	{
		return this.requiresNewFrame();
	}

	private boolean requiresNewFrame()
	{
		return (time - lastRecordTime > RENDER_UPDATE_TIME);
	}

	@Override
	public void jmeUpdate(float tpf)
	{

	}

	@Override
	public void jfxUdate(long time)
	{
		this.time = time;
		if (this.needsToRender.get())
		{
			lastRecordTime = time;
			this.needsToRender.set(false);
			int width, height;
			synchronized (this.imageView)
			{
				width = vpWidth();
				height = vpHeight();
				this.pixels = this.imageRender.getCameraImageData(this.viewportName, pixels);
			}
			pixelWriter.setPixels(0, 0,
					width, height,
					PixelFormat.getByteRgbInstance(),
					pixels, 0,
					vpWidth() * 3);
		}
	}

	private int vpHeight()
	{
		return imageRender.getViewportHeight(this.viewportName);
	}

	private int vpWidth()
	{
		return imageRender.getViewportWidth(this.viewportName);
	}

}
