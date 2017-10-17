package com.massisframework.massis3.commons.loader.animation.json;

import java.util.Arrays;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Clip implements Cloneable {

	@SerializedName("fps")
	@Expose
	private Integer fps;
	@SerializedName("description")
	@Expose
	private String description;
	@SerializedName("duration_frames")
	@Expose
	private Integer durationFrames;
	@SerializedName("max_length")
	@Expose
	private Integer maxLength;
	@SerializedName("min_length")
	@Expose
	private Integer minLength;
	@SerializedName("estimated_length")
	@Expose
	private Integer estimatedLength;
	@SerializedName("skeleton-root")
	@Expose
	private String skeletonRoot;
	@SerializedName("motion-root")
	@Expose
	private String motionRoot;
	@SerializedName("tposer-orientation")
	@Expose
	private float[] tposerOrientation = new float[4];

	/**
	 * 
	 * @return The fps
	 */
	public Integer getFps()
	{
		return fps;
	}

	/**
	 * 
	 * @param fps
	 *            The fps
	 */
	public void setFps(final Integer fps)
	{
		this.fps = fps;
	}

	/**
	 * 
	 * @return The description
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * 
	 * @param description
	 *            The description
	 */
	public void setDescription(final String description)
	{
		this.description = description;
	}

	/**
	 * 
	 * @return The durationFrames
	 */
	public Integer getDurationFrames()
	{
		return durationFrames;
	}

	/**
	 * 
	 * @param durationFrames
	 *            The duration_frames
	 */
	public void setDurationFrames(final Integer durationFrames)
	{
		this.durationFrames = durationFrames;
	}

	/**
	 * 
	 * @return The maxLength
	 */
	public Integer getMaxLength()
	{
		return maxLength;
	}

	/**
	 * 
	 * @param maxLength
	 *            The max_length
	 */
	public void setMaxLength(final Integer maxLength)
	{
		this.maxLength = maxLength;
	}

	/**
	 * 
	 * @return The minLength
	 */
	public Integer getMinLength()
	{
		return minLength;
	}

	/**
	 * 
	 * @param minLength
	 *            The min_length
	 */
	public void setMinLength(final Integer minLength)
	{
		this.minLength = minLength;
	}

	/**
	 * 
	 * @return The estimatedLength
	 */
	public Integer getEstimatedLength()
	{
		return estimatedLength;
	}

	/**
	 * 
	 * @param estimatedLength
	 *            The estimated_length
	 */
	public void setEstimatedLength(final Integer estimatedLength)
	{
		this.estimatedLength = estimatedLength;
	}

	/**
	 * 
	 * @return The skeletonRoot
	 */
	public String getSkeletonRoot()
	{
		return skeletonRoot;
	}

	/**
	 * 
	 * @param skeletonRoot
	 *            The skeleton-root
	 */
	public void setSkeletonRoot(final String skeletonRoot)
	{
		this.skeletonRoot = skeletonRoot;
	}

	/**
	 * 
	 * @return The motionRoot
	 */
	public String getMotionRoot()
	{
		return motionRoot;
	}

	/**
	 * 
	 * @param motionRoot
	 *            The motion-root
	 */
	public void setMotionRoot(final String motionRoot)
	{
		this.motionRoot = motionRoot;
	}

	/**
	 * 
	 * @return The tposerOrientation
	 */
	public float[] getTposerOrientation()
	{
		return tposerOrientation;
	}

	/**
	 * 
	 * @param tposerOrientation
	 *            The tposer-orientation
	 */
	public void setTposerOrientation(final float[] tposerOrientation)
	{
		this.tposerOrientation = tposerOrientation;
	}

	@Override
	protected Clip clone()
	{
		final Clip c = new Clip();
		c.fps = fps;
		c.description = description;
		c.durationFrames = durationFrames;
		c.maxLength = maxLength;
		c.minLength = minLength;
		c.estimatedLength = estimatedLength;
		c.skeletonRoot = skeletonRoot;
		c.motionRoot = motionRoot;
		c.tposerOrientation = Arrays.copyOf(tposerOrientation,
				tposerOrientation.length);
		return c;
	}

}
