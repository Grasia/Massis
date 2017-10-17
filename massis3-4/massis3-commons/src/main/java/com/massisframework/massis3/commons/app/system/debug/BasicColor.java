package com.massisframework.massis3.commons.app.system.debug;

import java.util.concurrent.ThreadLocalRandom;

import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Spatial;
import com.massisframework.massis3.commons.spatials.Materials;

public enum BasicColor {
	/**
	 * The color black (0,0,0).
	 */
	Black(new ColorRGBA(0f, 0f, 0f, 0.8f)),
	/**
	 * The color white (1,1,1).
	 */
	White(new ColorRGBA(1f, 1f, 1f, 0.8f)),
	/**
	 * The color gray (.2,.2,.2).
	 */
	DarkGray(new ColorRGBA(0.2f, 0.2f, 0.2f, 0.8f)),
	/**
	 * The color gray (.5,.5,.5).
	 */
	Gray(new ColorRGBA(0.5f, 0.5f, 0.5f, 0.8f)),
	/**
	 * The color gray (.8,.8,.8).
	 */
	LightGray(new ColorRGBA(0.8f, 0.8f, 0.8f, 0.8f)),
	/**
	 * The color red (1,0,0).
	 */
	Red(new ColorRGBA(1f, 0f, 0f, 0.8f)),
	/**
	 * The color green (0,1,0).
	 */
	Green(new ColorRGBA(0f, 1f, 0f, 0.8f)),
	/**
	 * The color blue (0,0,1).
	 */
	Blue(new ColorRGBA(0f, 0f, 1f, 0.8f)),
	/**
	 * The color yellow (1,1,0).
	 */
	Yellow(new ColorRGBA(1f, 1f, 0f, 0.8f)),
	/**
	 * The color magenta (1,0,1).
	 */
	Magenta(new ColorRGBA(1f, 0f, 1f, 0.8f)),
	/**
	 * The color cyan (0,1,1).
	 */
	Cyan(new ColorRGBA(0f, 1f, 1f, 0.8f)),
	/**
	 * The color orange (251/255, 130/255,0).
	 */
	Orange(new ColorRGBA(251f / 255f, 130f / 255f, 0f, 0.8f)),
	/**
	 * The color brown (65/255, 40/255, 25/255).
	 */
	Brown(new ColorRGBA(65f / 255f, 40f / 255f, 25f / 255f, 0.8f)),
	/**
	 * The color pink (1, 0.68, 0.68).
	 */
	Pink(new ColorRGBA(1f, 0.68f, 0.68f, 0.8f)),
	/**
	 * The black color with no alpha (0, 0, 0, 0).
	 */
	// BlackNoAlpha(new ColorRGBA(0f, 0f, 0f, 0f));
	;

	private Material materialFilled;
	private Material materialWired;

	BasicColor(final ColorRGBA color)
	{
		this.materialFilled = Materials.newUnshaded(color, false);
		this.materialWired = Materials.newUnshaded(color, true);
		this.materialFilled.getAdditionalRenderState()
				.setBlendMode(BlendMode.Alpha);
	}

	public void applyTo(final Spatial sp)
	{
		this.applyTo(sp, false);
	}

	public void applyTo(final Spatial sp, final boolean wire)
	{

		sp.setMaterial(wire ? this.materialWired : this.materialFilled);

	}

	public static BasicColor randomBasicColor()
	{
		return BasicColor.values()[ThreadLocalRandom.current()
				.nextInt(BasicColor.values().length)];
	}
}
