package com.massisframework.massis3.commons.loader.animation.json;

import java.util.Arrays;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

@Generated("org.jsonschema2pojo")
public class AnimationFrame implements Cloneable {

	@SerializedName("type")
	@Expose
	private String type;
	@SerializedName("time")
	@Expose
	private float time;
	@SerializedName("index")
	@Expose
	private int index;
	@SerializedName("segment")
	@Expose
	private Segment segment;
	@SerializedName("data")
	@Expose
	private float[] data;

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
	 * @return The time
	 */
	public float getTime()
	{
		return time;
	}

	/**
	 * 
	 * @param time
	 *            The time
	 */
	public void setTime(final int time)
	{
		this.time = time;
	}

	/**
	 * 
	 * @return The index
	 */
	public int getIndex()
	{
		return index;
	}

	/**
	 * 
	 * @param index
	 *            The index
	 */
	public void setIndex(final int index)
	{
		this.index = index;
	}

	/**
	 * 
	 * @return The segment
	 */
	public Segment getSegment()
	{
		return segment;
	}

	/**
	 * 
	 * @param segment
	 *            The segment
	 */
	public void setSegment(final Segment segment)
	{
		this.segment = segment;
	}

	public void getData(final int offset, final Quaternion store)
	{
		store.set(
				this.data[offset],
				this.data[offset + 1],
				this.data[offset + 2],
				this.data[offset + 3]);
	}

	public void getData(final int offset, final Vector3f store)
	{
		store.set(
				this.data[offset],
				this.data[offset + 1],
				this.data[offset + 2]);
	}

	/**
	 * 
	 * @param data
	 *            The data
	 */
	public void setData(final float[] data)
	{
		this.data = data;
	}

	@Override
	protected AnimationFrame clone()
	{
		final AnimationFrame af = new AnimationFrame();
		af.type = type;
		af.time = time;
		af.index = index;
		af.segment = segment.clone();
		af.data = Arrays.copyOf(this.data, this.data.length);
		return af;
	}

	// static void writeClass(Object obj, OutputCapsule oc) throws IOException
	// {
	// Field[] fields = obj.getClass().getDeclaredFields();
	// for (int i = 0; i < fields.length; i++)
	// {
	// Field field = fields[i];
	// SerializedName ann = fields[i].getAnnotation(SerializedName.class);
	// if (ann == null)
	// continue;
	// String name = ann.value();
	// Object fValue = null;
	// try
	// {
	// fValue = field.get(obj);
	// } catch (IllegalArgumentException | IllegalAccessException e)
	// {
	// e.printStackTrace();
	// }
	// if (Savable.class.isAssignableFrom(field.getType()))
	// {
	// oc.write((Savable) fValue, name, null);
	//
	// } else if (List.class.isAssignableFrom(field.getType()))
	// {
	// oc.writeSavableArrayList(new ArrayList<>((List)fValue), name, defVal);
	// }
	//
	// }
	// }

}
