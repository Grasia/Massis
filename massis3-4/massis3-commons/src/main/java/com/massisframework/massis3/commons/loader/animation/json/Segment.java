
package com.massisframework.massis3.commons.loader.animation.json;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Segment implements Cloneable {

	@SerializedName("length")
	@Expose
	private int length;
	@SerializedName("last")
	@Expose
	private String last;
	@SerializedName("transition_start")
	@Expose
	private int transitionStart;
	@SerializedName("transition_pivots")
	@Expose
	private String transitionPivots;

	/**
	 * 
	 * @return The length
	 */
	public int getLength()
	{
		return length;
	}

	/**
	 * 
	 * @param length
	 *            The length
	 */
	public void setLength(final int length)
	{
		this.length = length;
	}

	/**
	 * 
	 * @return The last
	 */
	public String getLast()
	{
		return last;
	}

	/**
	 * 
	 * @param last
	 *            The last
	 */
	public void setLast(final String last)
	{
		this.last = last;
	}

	/**
	 * 
	 * @return The transitionStart
	 */
	public int getTransitionStart()
	{
		return transitionStart;
	}

	/**
	 * 
	 * @param transitionStart
	 *            The transition_start
	 */
	public void setTransitionStart(final int transitionStart)
	{
		this.transitionStart = transitionStart;
	}

	/**
	 * 
	 * @return The transitionPivots
	 */
	public String getTransitionPivots()
	{
		return transitionPivots;
	}

	/**
	 * 
	 * @param transitionPivots
	 *            The transition_pivots
	 */
	public void setTransitionPivots(final String transitionPivots)
	{
		this.transitionPivots = transitionPivots;
	}

	@Override
	public Segment clone()
	{
		final Segment s = new Segment();
		s.length = length;
		s.last = last;
		s.transitionStart = transitionStart;
		s.transitionPivots = transitionPivots;
		return s;
	}

}
