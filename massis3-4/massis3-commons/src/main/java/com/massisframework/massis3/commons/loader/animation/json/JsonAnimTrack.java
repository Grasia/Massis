package com.massisframework.massis3.commons.loader.animation.json;

import java.util.Arrays;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

public class JsonAnimTrack implements Cloneable {
	private float[] times;
	private Vector3f[] translations;
	private Quaternion[] rotations;
	private String boneName;

	public JsonAnimTrack(final float[] times, final Vector3f[] translations,
			final Quaternion[] rotations, final String boneName)
	{
		this.times = times;
		this.translations = translations;
		this.rotations = rotations;
		this.boneName = boneName;
	}

	public float[] getTimes()
	{
		return times;
	}

	public void setTimes(final float[] times)
	{
		this.times = times;
	}

	public Vector3f[] getTranslations()
	{
		return translations;
	}

	public void setTranslations(final Vector3f[] translations)
	{
		this.translations = translations;
	}

	public Quaternion[] getRotations()
	{
		return rotations;
	}

	public void setRotations(final Quaternion[] rotations)
	{
		this.rotations = rotations;
	}

	public String getBoneName()
	{
		return boneName;
	}

	public void setBoneIndex(final String boneName)
	{
		this.boneName = boneName;
	}

	@Override
	protected JsonAnimTrack clone()
	{
		return new JsonAnimTrack(Arrays.copyOf(times, times.length),
				Arrays.stream(this.translations).map(Vector3f::new)
						.toArray(s -> new Vector3f[s]),
				Arrays.stream(this.rotations).map(Quaternion::new)
						.toArray(s -> new Quaternion[s]),
				this.boneName);
	}
}
