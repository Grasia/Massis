package com.massisframework.massis3.commons.spatials.mesh;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;

public class QuadMesh extends Mesh {

	private int[] indexes_arr;
	private int indexes_arr_size;

	private float[] normals_arr;
	private int normals_arr_size;

	private float[] points_arr;
	private int points_arr_size;

	private int point_count;

	 private final Map<Vector3f, Integer> pointIndexMap;
	//private ConstrainedPointMap<Integer> pointIndexMap;
	private int quads;

	
	public QuadMesh(
			int initialCap,
			final Vector3f min, final Vector3f max,
			final float cellSize)
	{

		this.indexes_arr = new int[initialCap];
		this.indexes_arr_size = 0;
		this.normals_arr = new float[initialCap];
		this.normals_arr_size = 0;
		this.points_arr = new float[initialCap];
		this.points_arr_size = 0;
		this.point_count = 0;
		this.pointIndexMap = new HashMap<>();//new ConstrainedPointMap<Integer>(Integer.class,min,max,cellSize);

	}
	public void addQuad(Vector3f quadCenter, final QuadDirection dir, final float quadWidth)
	{

		quadCenter = quadCenter.clone();
		if (pointIndexMap.containsKey(quadCenter)) return;

		/**
		 * <pre>
		^ 2----------3
		| | \        |
		| |   \      |
		Y |     \    |
		| |       \  |
		| |         \|
		v 0----------1
		 <------x----->
		 * 
		 * </pre>
		 */

		Vector3f storeP = new Vector3f();
		final Vector3f storeN = new Vector3f();

		storeP = dir.getPoint(0, storeP).multLocal(quadWidth).addLocal(quadCenter);
		dir.getNormal(0, storeN);
		final int p0 = getPoint(storeP, storeN);

		storeP = dir.getPoint(1, storeP).multLocal(quadWidth).addLocal(quadCenter);
		dir.getNormal(1, storeN);
		final int p1 = getPoint(storeP, storeN);

		storeP = dir.getPoint(2, storeP).multLocal(quadWidth).addLocal(quadCenter);
		dir.getNormal(2, storeN);
		final int p2 = getPoint(storeP, storeN);

		storeP = dir.getPoint(3, storeP).multLocal(quadWidth).addLocal(quadCenter);
		dir.getNormal(3, storeN);
		final int p3 = getPoint(storeP, storeN);

		addIndexes(p0, p1, p2, p3);
		this.quads++;

	}

	public void update()
	{

		final FloatBuffer posBuffer = BufferUtils
				.createFloatBuffer(this.points_arr_size);
		posBuffer.put(this.points_arr, 0, this.points_arr_size);
		this.setBuffer(Type.Position, 3, posBuffer);

		final FloatBuffer normBuffer = BufferUtils
				.createFloatBuffer(this.normals_arr_size);
		for (int i = 0; i < this.normals_arr_size; i++)
			normBuffer.put(this.normals_arr[i]);

		this.setBuffer(Type.Normal, 3, normBuffer);

		final IntBuffer indexBuffer = BufferUtils
				.createIntBuffer(this.indexes_arr_size);
		for (int i = 0; i < this.indexes_arr_size; i++)
			indexBuffer.put(this.indexes_arr[i]);
		this.setBuffer(Type.Index, 3, indexBuffer);

		this.updateBound();
		this.updateCounts();
		this.setStatic();
	}

	private void addIndexes(final int p0, final int p1, final int p2,
			final int p3)
	{

		this.addIndexBlock(p0, p1, p2, p0, p2, p3);

	}

	private void addIndexBlock(final int... ps)
	{
		for (int i = 0; i < ps.length; i++)
		{
			this.addIndexUnit(ps[i]);
		}
	}

	private void addNormalsBlock(final float... ns)
	{
		for (int i = 0; i < ns.length; i++)
		{
			this.addNormalUnit(ns[i]);
		}
	}

	private void addPointsBlock(final float... ns)
	{
		for (int i = 0; i < ns.length; i++)
		{
			this.addPointUnit(ns[i]);
		}
	}

	private void addPointUnit(final float item)
	{
		this.points_arr_size++;
		this.points_arr = ensureSize(points_arr, this.points_arr_size);
		this.points_arr[points_arr_size - 1] = item;

	}

	private void addIndexUnit(final int item)
	{
		this.indexes_arr_size++;
		this.indexes_arr = ensureSize(indexes_arr, indexes_arr_size);
		this.indexes_arr[indexes_arr_size - 1] = item;
	}

	private void addNormalUnit(final float item)
	{
		this.normals_arr_size++;
		this.normals_arr = ensureSize(normals_arr, this.normals_arr_size);
		this.normals_arr[normals_arr_size - 1] = item;
	}

	private int getPoint(Vector3f p, final Vector3f normal)
	{
		p = p.clone();
		Integer index = pointIndexMap.get(p);
		if (index == null)
		{
			index = this.point_count;
			this.pointIndexMap.put(p.clone(), this.point_count);
			this.point_count++;
			this.addPointsBlock(p.x, p.y, p.z);
			this.addNormalsBlock(normal.x, normal.y, normal.z);
		}
		return index;
	}

	// Utility methods
	private static int[] ensureSize(final int[] arr, final int size)
	{
		if (size >= arr.length)
		{
			int newSize = arr.length;
			while (newSize <= size)
				newSize *= 2;
			final int[] newArray = new int[newSize];
			System.arraycopy(arr, 0, newArray, 0, arr.length);
			return newArray;
		}
		return arr;
	}

	private static float[] ensureSize(final float[] arr, final int size)
	{
		if (size >= arr.length)
		{
			int newSize = arr.length;
			while (newSize <= size)
				newSize *= 2;
			final float[] newArray = new float[newSize];
			System.arraycopy(arr, 0, newArray, 0, arr.length);
			return newArray;
		}
		return arr;
	}

	public int getNumQuads()
	{
		return quads;
	}
}