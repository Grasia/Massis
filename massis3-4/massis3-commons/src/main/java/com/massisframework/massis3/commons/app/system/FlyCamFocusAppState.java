package com.massisframework.massis3.commons.app.system;

import com.jme3.app.Application;
import com.jme3.app.FlyCamAppState;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;

public class FlyCamFocusAppState extends AbstractAppState implements ActionListener {

	private InputManager inputManager;

	private static final String KEY_TAB = "KEY_TAB_" + FlyCamFocusAppState.class;

	private AppStateManager stateManager;

	@Override
	public void initialize(AppStateManager stateManager, Application app)
	{
		this.stateManager = stateManager;
		this.inputManager = app.getInputManager();
		this.registerInputs();
		super.initialize(stateManager, app);
	}

	private void registerInputs()
	{
		this.inputManager.addMapping(KEY_TAB, new KeyTrigger(KeyInput.KEY_TAB));
		this.inputManager.addListener(this, KEY_TAB);
	}

	@Override
	public void onAction(String name, boolean isPressed, float tpf)
	{
		if (KEY_TAB.equals(name) && !isPressed)
		{
			FlyCamAppState fcs = stateManager.getState(FlyCamAppState.class);
			if (fcs != null)
			{
				fcs.setEnabled(!fcs.isEnabled());
			}
		}
	}

}
