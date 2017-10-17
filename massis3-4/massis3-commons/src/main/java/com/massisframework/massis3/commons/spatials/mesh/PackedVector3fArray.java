package com.massisframework.massis3.commons.spatials.mesh;

import com.jme3.math.Vector3f;

public final class PackedVector3fArray {

	private final float[] packed;
	private final int width;
	private final int height;
	private final int depth;

	public PackedVector3fArray(final int width, final int height,
			final int depth)
	{
		this.width = width;
		this.height = height;
		this.depth = depth;
		this.packed = new float[width * height * depth * 3];
	}

	private final int getIndex(final int i, final int j, final int k)
	{
		return i * height * depth + j * depth + k;
	}

	public Vector3f get(final int i, final int j, final int k,
			final Vector3f store)
	{
		final int index = getIndex(i, j, k);
		return store.set(
				this.packed[index * 3 + 0],
				this.packed[index * 3 + 1],
				this.packed[index * 3 + 2]);
	}

	public float getX(final int i, final int j, final int k)
	{
		final int index = getIndex(i, j, k);
		return this.packed[index * 3 + 0];
	}

	public final float getY(final int i, final int j, final int k)
	{
		final int index = getIndex(i, j, k);
		return this.packed[index * 3 + 1];
	}

	public final float getZ(final int i, final int j, final int k)
	{
		final int index = getIndex(i, j, k);
		return this.packed[index * 3 + 2];
	}

	public final void set(final int i, final int j, final int k, final float x,
			final float y, final float z)
	{
		this.packed[i * 3 + 0] = x;
		this.packed[i * 3 + 1] = y;
		this.packed[i * 3 + 2] = z;
	}

	public final void set(final int i, final int j, final int k,
			final Vector3f data)
	{
		this.set(i, j, k, data.x, data.y, data.z);
	}

	public final void clear()
	{
		for (int i = 0; i < packed.length; i++)
		{
			packed[i] = 0f;
		}
	}

}
