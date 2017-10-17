
package com.massisframework.massis3.commons.loader.animation.json;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class FrameDescriptor implements Cloneable {

	public static enum ChangeType {
		POSITION, ROTATION
	}

	@SerializedName("node")
	@Expose
	private String node;
	@SerializedName("ch")
	@Expose
	private String ch;
	@SerializedName("offset")
	@Expose
	private int offset;

	/**
	 * 
	 * @return The node
	 */
	public String getNode()
	{
		return node;
	}

	/**
	 * 
	 * @param node
	 *            The node
	 */
	public void setNode(final String node)
	{
		this.node = node;
	}

	/**
	 * 
	 * @return The ch
	 */
	public ChangeType getCh()
	{
		return "rot".equals(this.ch) ? ChangeType.ROTATION
				: ChangeType.POSITION;
	}

	/**
	 * 
	 * @param ch
	 *            The ch
	 */
	public void setCh(final String ch)
	{
		this.ch = ch;
	}

	/**
	 * 
	 * @return The offset
	 */
	public int getOffset()
	{
		return offset;
	}

	/**
	 * 
	 * @param offset
	 *            The offset
	 */
	public void setOffset(final int offset)
	{
		this.offset = offset;
	}

	@Override
	protected FrameDescriptor clone()
	{
		final FrameDescriptor fd = new FrameDescriptor();
		fd.node = node;
		fd.ch = ch;
		fd.offset = offset;
		return fd;
	}

}
