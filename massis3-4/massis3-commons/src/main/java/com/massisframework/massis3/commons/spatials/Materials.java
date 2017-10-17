package com.massisframework.massis3.commons.spatials;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.system.JmeSystem;

public class Materials {

	public static final String UNSHADED = "Common/MatDefs/Misc/Unshaded.j3md";
	public static final String LIGHTING = "Common/MatDefs/Light/Lighting.j3md";

	private static final AssetManager am = newAssetManager();

	public static AssetManager newAssetManager()
	{
		return JmeSystem.newAssetManager(JmeSystem.getPlatformAssetConfigURL());
	}

	public static Material newUnshaded()
	{
		return newUnshaded(am);
	}

	public static Material newUnshaded(final ColorRGBA color)
	{
		return newUnshaded(am, color);
	}

	public static Material newUnshaded(final ColorRGBA color,
			final boolean wireframe)
	{
		return newUnshaded(am, color, wireframe);
	}

	public static Material newUnshaded(final AssetManager am,
			final ColorRGBA color)
	{
		return newUnshaded(am, color, false);
	}

	public static Material newUnshaded(final AssetManager am,
			final ColorRGBA color,
			final boolean wireframe)
	{
		final Material m = Materials.newUnshaded(am);
		m.setColor("Color", color);
		m.getAdditionalRenderState().setWireframe(wireframe);
		return m;
	}

	public static Material newUnshaded(final AssetManager am)
	{
		return new Material(am, UNSHADED);
	}

	public static Material newVertexColorMaterial()
	{
		Material vertexMat = newUnshaded();
		vertexMat.setBoolean("VertexColor", true);
		return vertexMat;
	}

	public static Material newLighting()
	{
		return new Material(am, LIGHTING);
	}

	public static Material newLighting(final AssetManager am)
	{
		return new Material(am, LIGHTING);
	}

}
