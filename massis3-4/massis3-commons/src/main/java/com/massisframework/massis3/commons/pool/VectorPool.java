package com.massisframework.massis3.commons.pool;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;

public class VectorPool extends GenericTempVar {

	private final SimplePool<Vector3f> v3pool;
	private final SimplePool<Vector2f> v2pool;

	public VectorPool(final ReleaseAble tempVars)
	{
		super(tempVars);
		this.v3pool = new SimplePool<>(Vector3f.class);
		this.v2pool = new SimplePool<>(Vector2f.class);
	}

	@Override
	public void release()
	{
		super.release();
		this.v3pool.release();
		this.v2pool.release();
	}

	public static class TestK {
		static int counter = 0;
		int id = counter++;

		@Override
		public String toString()
		{
			return "TestK [id=" + id + "]";
		}
	}

	public Vector3f n3f()
	{
		return this.v3pool.get();
	}

	public Vector2f n2f()
	{
		return this.v2pool.get();
	}

}
