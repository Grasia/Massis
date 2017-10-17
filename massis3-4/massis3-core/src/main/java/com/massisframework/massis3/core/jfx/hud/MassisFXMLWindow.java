package com.massisframework.massis3.core.jfx.hud;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.jme3.app.state.AppStateManager;
import com.jme3x.jfx.window.FXMLWindow;

public class MassisFXMLWindow<C extends JFXJmeController> extends FXMLWindow<C> {

	private CompletableFuture<MassisFXMLWindow<C>> readyCF = new CompletableFuture<>();

	@Override
	protected URL getWindowRessource()
	{
		try
		{
			return getClass().getResource("MassisFXMLWindow.fxml");
		} catch (Exception e)
		{
			this.readyCF.completeExceptionally(e);
			throw new RuntimeException(e);
		}
	}

	public MassisFXMLWindow(URL location) throws IOException
	{
		super(location, location.openStream());
	}

	@Override
	protected URL getWindowCss()
	{
		try
		{
			return getClass().getResource("MassisFXMLWindow.css");
		} catch (Exception e)
		{
			this.readyCF.completeExceptionally(e);
			throw new RuntimeException(e);
		}
	}

	public void jmeUpdate(float tpf)
	{
		this.getController().jmeUpdate(tpf);
	}

	public void jmeInitialize(AppStateManager stateManager)
	{
		this.getController().jmeInitialize(stateManager);
	}

	public void jfxUdate(long now)
	{
		this.getController().jfxUdate(now);
	}

	public CompletionStage<MassisFXMLWindow<C>> ready()
	{
		return this.readyCF;
	}

	@Override
	protected void afterInit()
	{
		this.readyCF.complete(this);
	}

}
