package com.massisframework.massis3.core.systems.gui;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.input.InputManager;
import com.jme3.scene.Node;
import com.jme3x.jfx.AbstractHud;
import com.jme3x.jfx.GuiManager;
import com.jme3x.jfx.window.AbstractWindow;
import com.jme3x.jfx.window.FXMLWindow;
import com.jme3x.jfx.windowpos.IRememberMeService;
import com.massisframework.massis3.core.jfx.annotations.FXMLPath;
import com.massisframework.massis3.core.jfx.hud.JFXJmeController;
import com.massisframework.massis3.core.jfx.hud.MassisFXMLWindow;

import javafx.animation.AnimationTimer;
import javafx.stage.Stage;

@Deprecated
public class JavaFXSystem extends AbstractAppState {

	private static final Logger log = LoggerFactory.getLogger(JavaFXSystem.class);
	private SimpleApplication app;
	private GuiManager guiManager;
	private AssetManager assetManager;
	private Node guiNode;
	private InputManager inputManager;
	private AttachListener rememberService;
	private AppStateManager stateManager;
	private CopyOnWriteArrayList<Consumer<Float>> jmeListeners;

	@Override
	public void initialize(AppStateManager stateManager, Application app)
	{
		this.app = (SimpleApplication) app;
		this.guiNode = this.app.getGuiNode();
		this.assetManager = this.app.getAssetManager();
		this.inputManager = this.app.getInputManager();
		this.stateManager = stateManager;
		this.rememberService = new AttachListener();
		this.jmeListeners = new CopyOnWriteArrayList<>();
		this.initializeJFX();
		super.initialize(stateManager, app);
	}

	private void initializeJFX()
	{
		this.guiManager = new GuiManager(
				this.guiNode,
				this.assetManager,
				this.app,
				true,
				null);
		this.guiManager.setRememberMeService(this.rememberService);
		this.inputManager.addRawInputListener(this.guiManager.getInputRedirector());
	}

	public <ControllerType extends JFXJmeController> CompletionStage<FXMLWindow<ControllerType>> createWindow(
			Class<ControllerType> controllerClass)
	{
		CompletableFuture<FXMLWindow<ControllerType>> cF = new CompletableFuture<>();
		createWindow_internal(controllerClass, cF);
		return cF;
	}

	private <ControllerType extends JFXJmeController> void createWindow_internal(
			Class<ControllerType> controllerClass,
			CompletableFuture<FXMLWindow<ControllerType>> cF)
	{
		FXMLPath ann = controllerClass.getAnnotation(FXMLPath.class);
		if (ann == null)
		{
			throw new RuntimeException("@FXMLPath annotation not present in " + controllerClass);
		}
		String path = ann.value();
		URL resource = controllerClass.getResource(path);
		try
		{
			MassisFXMLWindow<ControllerType> window = new MassisFXMLWindow<>(resource);
			window.precache();
			window.ready().thenAccept(_w -> {
				window.externalized().set(true);
				try
				{
					window.jmeInitialize(stateManager);
				} catch (Exception e)
				{
					log.error("Error when jme-initializing window", e);
				}
				AnimationTimer timer = new AnimationTimer() {
					@Override
					public void handle(long now)
					{
						window.jfxUdate(now);
					}
				};
				Consumer<Float> jmeConsumer = (tpf) -> window.jmeUpdate(tpf);
				this.rememberService.setRemoveListener(window, () -> {
					timer.stop();
					jmeListeners.remove(jmeConsumer);
				});
				jmeListeners.add(jmeConsumer);
				timer.start();
				cF.complete(window);
			});
			this.guiManager.attachHudAsync(window);
		} catch (IOException e)
		{
			log.error("Error when loading window", e);
		}
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
	}

	@Override
	public void update(float tpf)
	{
		this.jmeListeners.forEach(l -> l.accept(tpf));
	}

	private class AttachListener implements IRememberMeService {

		private Map<AbstractHud, Runnable> attachListeners = new ConcurrentHashMap<>();
		private Map<AbstractHud, Runnable> removeListeners = new ConcurrentHashMap<>();
		private Map<AbstractHud, Runnable> onExternalListeners = new ConcurrentHashMap<>();

		@Override
		public void onAttach(AbstractHud hud)
		{
			Runnable action = this.attachListeners.remove(hud);
			if (action != null)
			{
				action.run();
			}
		}

		public void setAttachListener(AbstractHud hud, Runnable action)
		{
			this.attachListeners.put(hud, action);
		}

		@Override
		public void onRemove(AbstractHud hud)
		{
			Runnable action = this.removeListeners.remove(hud);
			if (action != null)
			{
				action.run();
			}
		}

		public void setRemoveListener(AbstractHud hud, Runnable action)
		{
			this.removeListeners.put(hud, action);
		}

		@Override
		public void onExternal(AbstractWindow window, Stage externalStage)
		{
			Runnable action = this.onExternalListeners.remove(window);
			if (action != null)
			{
				action.run();
			}
		}

		@SuppressWarnings("unused")
		public void setExternalListener(AbstractHud hud, Runnable action)
		{
			this.onExternalListeners.put(hud, action);
		}
	}

}
