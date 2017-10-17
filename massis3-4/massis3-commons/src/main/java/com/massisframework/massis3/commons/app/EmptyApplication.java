package com.massisframework.massis3.commons.app;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.animation.SkeletonControl;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext;

public class EmptyApplication extends SimpleApplication {

	private final CompletableFuture<EmptyApplication> readyCF;
	private static final Logger log = LoggerFactory.getLogger(EmptyApplication.class);

	public EmptyApplication(List<AppState> initialStates)
	{
		super(getConfiguredStates(initialStates));
		this.readyCF = new CompletableFuture<>();
	}

	@Override
	public void simpleInitApp()
	{
		this.setPauseOnLostFocus(false);
	}

	private static AppState[] getConfiguredStates(List<AppState> initialStates)
	{
		//List<AppState> sorted = AppStateTopologicalSorter.sort(initialStates);
		List<AppState> sorted=new ArrayList<>(initialStates);
		sorted.add(new InitializationMonitorAppState());
		return sorted.toArray(new AppState[0]);
	}

	public void waitUntilStarted()
	{
		try
		{
			this.readyCF.get();
		} catch (InterruptedException | ExecutionException e)
		{
			log.error("Error while waiting for Application start", e);
		}
	}

	public CompletionStage<EmptyApplication> readyStage()
	{
		return this.readyCF;
	}

	public void setSpeed(float speed)
	{
		this.speed = speed;
	}

	private static final java.util.logging.Logger skeletonLogger = java.util.logging.Logger
			.getLogger(SkeletonControl.class.getName());

	@Override
	public void simpleUpdate(final float tpf)
	{
		super.simpleUpdate(tpf);

	}

	@Override
	public void update()
	{
		super.update();
		if (skeletonLogger.getLevel() != java.util.logging.Level.OFF)
		{
			skeletonLogger.setLevel(java.util.logging.Level.OFF);
		}
	}

	public static CompletionStage<EmptyApplication> run(
			JmeContext.Type contextType,
			AppSettings settings,
			List<AppState> initialStates)
	{
		CompletableFuture<EmptyApplication> cF2 = new CompletableFuture<>();
		Thread thread = new Thread(() -> {
			EmptyApplication app = new EmptyApplication(initialStates);
			app.setSettings(settings);
			app.start(contextType, true);
			app.readyStage().thenAccept(cF2::complete);
		});
		thread.setName("EmptyApplication Wrapper Thread");
		thread.start();
		return cF2;
	}

	private static class InitializationMonitorAppState extends AbstractAppState {

		@Override
		public void initialize(AppStateManager stateManager, Application app)
		{
			super.initialize(stateManager, app);
			EmptyApplication emptyApp = (EmptyApplication) app;
			emptyApp.readyCF.complete(emptyApp);
			stateManager.detach(this);
		}

	}

}
