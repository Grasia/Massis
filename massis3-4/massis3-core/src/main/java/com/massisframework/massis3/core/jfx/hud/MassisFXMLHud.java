package com.massisframework.massis3.core.jfx.hud;

import java.net.URL;
import java.util.ResourceBundle;

import com.jme3x.jfx.AbstractHud;
import com.jme3x.jfx.window.FXMLControllerFactoryHook;

import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.layout.Region;

public class MassisFXMLHud<ControllerType> extends AbstractHud {

	private URL location;
	private ControllerType controller;

	public MassisFXMLHud(final URL location)
	{
		this.location = location;
	}

	@Override
	protected Region innerInit() throws Exception
	{
		final FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setLocation(location);
		final ResourceBundle defaultRessources = fxmlLoader.getResources();
		fxmlLoader.setResources(this.addCustomRessources(defaultRessources));
		fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
		fxmlLoader.setControllerFactory(FXMLControllerFactoryHook.getFactory());
		final Region rv = fxmlLoader.load(location.openStream());
		this.controller = fxmlLoader.getController();
		return rv;
	}

	//
	public ControllerType getController()
	{
		return this.controller;
	}

	//
	// /**
	// * Hook to add own Resourcebundles if necessary
	// *
	// * @param defaultRessources
	// * the currently set value
	// * @return
	// */
	protected ResourceBundle addCustomRessources(final ResourceBundle defaultRessources)
	{
		return defaultRessources;
	}
}
