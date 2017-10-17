package com.massisframework.massis3.commons.app;

import java.util.concurrent.CopyOnWriteArrayList;

import com.jme3.app.Application;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.renderer.RenderManager;

public class AppStateManagerGroupAppState implements AppState {

	private AppStateManager parentStateManager;
	private Application parentApplication;
	private CopyOnWriteArrayList<Application> subApplications;
	private boolean initialized;
	private boolean enabled;

	public AppStateManagerGroupAppState()
	{
		this.subApplications = new CopyOnWriteArrayList<>();
		this.initialized = false;
	}

	@Override
	public void initialize(AppStateManager stateManager, Application app)
	{
		this.parentStateManager = stateManager;
		this.parentApplication = app;
		this.initialized = true;
	}

	public void addSubApplication(Application subApplication)
	{
		this.subApplications.add(subApplication);
	}

	@Override
	public boolean isInitialized()
	{
		return this.initialized;
	}

	@Override
	public void setEnabled(boolean active)
	{
		this.enabled = active;
	}

	@Override
	public boolean isEnabled()
	{
		return this.enabled;
	}

	@Override
	public void stateAttached(AppStateManager stateManager)
	{
		this.parentStateManager = stateManager;
	}

	@Override
	public void stateDetached(AppStateManager stateManager)
	{

	}

	@Override
	public void update(float tpf)
	{

	}

	@Override
	public void render(RenderManager rm)
	{

	}

	@Override
	public void postRender()
	{

	}

	@Override
	public void cleanup()
	{

	}

}
