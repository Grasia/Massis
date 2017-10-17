package com.massisframework.massis3.commons.spatials;

import javax.vecmath.Vector2f;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;

public interface Vector3fHolder {

	public float getX();

	public float getY();

	public float getZ();

	public default Vector3f get()
	{
		return get(new Vector3f());
	}

	public default Vector3f get(Vector3f store)
	{
		return store.set(getX(), getY(), getZ());
	}

	public default float distance(final float x, final float y, final float z)
	{
		final double dx = getX() - x;
		final double dy = getY() - y;
		final double dz = getZ() - z;
		return (float) (dx * dx + dy * dy + dz * dz);
	}

	public default float distanceSquared2D(final float x, final float z)
	{
		final double dx = getX() - x;
		final double dy = getZ() - z;
		return (float) (dx * dx + dy * dy);
	}

	public default float distanceSquared2D(final Vector3f v)
	{
		final double dx = getX() - v.x;
		final double dy = getZ() - v.z;
		return (float) (dx * dx + dy * dy);
	}

	public default Vector2D getAsVector2D()
	{
		return new Vector2D(getX(), getZ());
	}

	public default Vector2f getAsVector2f()
	{
		return this.getAsVector2f(new Vector2f());
	}

	public default Vector2f getAsVector2f(Vector2f store)
	{
		store.set(getX(), getZ());
		return store;
	}

	public default float distance2D(final Vector3f v)
	{
		return FastMath.sqrt(distanceSquared2D(v));
	}

	public default float distance2D(final Vector3fHolder lh)
	{
		return FastMath.sqrt(distanceSquared2D(lh.getX(), lh.getZ()));
	}
}
