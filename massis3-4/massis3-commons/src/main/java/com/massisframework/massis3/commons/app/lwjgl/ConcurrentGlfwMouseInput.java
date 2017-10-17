package com.massisframework.massis3.commons.app.lwjgl;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.jme3.input.lwjgl.GlfwMouseInput;
import com.jme3.system.lwjgl.LwjglWindow;

public class ConcurrentGlfwMouseInput extends GlfwMouseInput {

	public ConcurrentGlfwMouseInput(LwjglWindow context)
	{
		super(context);
		configureMouseEventsQueues();
	}

	// XXX Hack for making them concurrent
	private void configureMouseEventsQueues()
	{
		try
		{
			Field mMF = GlfwMouseInput.class.getDeclaredField("mouseMotionEvents");
			Field mBF = GlfwMouseInput.class.getDeclaredField("mouseButtonEvents");
			mMF.setAccessible(true);
			mBF.setAccessible(true);

			mMF.set(this, new ConcurrentLinkedDeque<>());
			mBF.set(this, new ConcurrentLinkedDeque<>());

		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public void destroy()
	{
		super.destroy();
	}

}
