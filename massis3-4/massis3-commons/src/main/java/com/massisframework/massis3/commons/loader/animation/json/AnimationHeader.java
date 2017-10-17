package com.massisframework.massis3.commons.loader.animation.json;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class AnimationHeader implements Cloneable {

	@SerializedName("type")
	@Expose
	private String type;
	@SerializedName("format")
	@Expose
	private String format;
	@SerializedName("clip")
	@Expose
	private Clip clip;
	@SerializedName("frame_descriptor")
	@Expose
	private List<FrameDescriptor> frameDescriptor = new ArrayList<FrameDescriptor>();

	/**
	 * 
	 * @return The type
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * 
	 * @param type
	 *            The type
	 */
	public void setType(final String type)
	{
		this.type = type;
	}

	/**
	 * 
	 * @return The format
	 */
	public String getFormat()
	{
		return format;
	}

	/**
	 * 
	 * @param format
	 *            The format
	 */
	public void setFormat(final String format)
	{
		this.format = format;
	}

	/**
	 * 
	 * @return The clip
	 */
	public Clip getClip()
	{
		return clip;
	}

	/**
	 * 
	 * @param clip
	 *            The clip
	 */
	public void setClip(final Clip clip)
	{
		this.clip = clip;
	}

	/**
	 * 
	 * @return The frameDescriptor
	 */
	public List<FrameDescriptor> getFrameDescriptor()
	{
		return frameDescriptor;
	}

	/**
	 * 
	 * @param frameDescriptor
	 *            The frame_descriptor
	 */
	public void setFrameDescriptor(final List<FrameDescriptor> frameDescriptor)
	{
		this.frameDescriptor = frameDescriptor;
	}

	@Override
	protected AnimationHeader clone()
	{
		final AnimationHeader ah = new AnimationHeader();
		ah.type = type;
		ah.format = format;
		ah.clip = clip.clone();
		ah.frameDescriptor = frameDescriptor.stream()
				.map(FrameDescriptor::clone).collect(Collectors.toList());
		return ah;
	}

}
