package com.massisframework.massis3.commons.editor;

import java.util.Arrays;

import com.eteks.sweethome3d.SweetHome3DWithPlugins;

public class SweetHome3DEditor {

	static
	{
		System.setProperty("com.eteks.sweethome3d.j3d.useOffScreen3DView",
				"true");
		System.setProperty("com.eteks.sweethome3d.no3D",
				"true");
	}

	public static void main(final String[] args)
	{
		SweetHome3DWithPlugins.run(args, Arrays.asList(/*ComponentPlugin.class*/));
	}

}
