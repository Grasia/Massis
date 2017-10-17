package com.massisframework.massis3.commons.collections.map;

import java.util.Map;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.math.Vector3f;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class ConstrainedPointMap<T> {

	private static final Logger log = LoggerFactory.getLogger(ConstrainedPointMap.class);

	private Map<Integer, T> map;

	private final int minX;
	private final int minY;
	private final int minZ;

	private final int maxX;
	private final int maxY;
	private final int maxZ;

	private final int multiplier;

	public final int i_length;
	public final int j_length;
	public final int k_length;

	public final int cellSize;

	public ConstrainedPointMap(
			final Class<T> type, final Vector3f min, final Vector3f max,
			final float cellSize)
	{
		this(type,
				min.x, min.y, min.z,
				max.x, max.y, max.z,
				cellSize);

	}

	public ConstrainedPointMap(
			final Class<T> type,
			final float minX, final float minY, final float minZ,
			final float maxX, final float maxY, final float maxZ,
			final float cellSize)
	{

		// HARDCODED
		this.multiplier = 10000;
		this.cellSize = (int) (cellSize * multiplier);

		this.minX = (int) (minX * multiplier - this.cellSize);
		this.minY = (int) (minY * multiplier - this.cellSize);
		this.minZ = (int) (minZ * multiplier - this.cellSize);

		this.maxX = (int) (maxX * multiplier + this.cellSize);
		this.maxY = (int) (maxY * multiplier + this.cellSize);
		this.maxZ = (int) (maxZ * multiplier + this.cellSize);

		this.i_length = (this.maxX - this.minX) / this.cellSize;
		this.j_length = (this.maxY - this.minY) / this.cellSize;
		this.k_length = (this.maxZ - this.minZ) / this.cellSize;

		this.map = new Int2ObjectOpenHashMap<>();// CollectionsFactory.createMap(Long.class,
													// type);
	}

	public T get(final Vector3f v)
	{
		return get(v.x, v.y, v.z);
	}

	public T put(final Vector3f v, final T value)
	{
		return put(v.x, v.y, v.z, value);
	}

	public boolean containsKey(final Vector3f v)
	{
		return containsKey(v.x, v.y, v.z);
	}

	public boolean containsKey(final float x, final float y, final float z)
	{
		return get(x, y, z) != null;
	}

	public T get(final float x, final float y, final float z)
	{
		final int uid = toGridIndex(x, y, z);
//		final int i = getI(x);
//		final int j = getJ(y);
//		final int k = getK(z);
//		
//		if (i<this.minX) return null;
//		if (i>this.maxX) return null;
//		
//		if (j<this.minY) return null;
//		if (j>this.maxY) return null;
//		
//		if (k<this.minZ) return null;
//		if (k>this.maxZ) return null;
		
		return this.map.get(uid);
	}

	public T put(final float x, final float y, final float z, final T value)
	{
		final int uid = toGridIndex(x, y, z);
		return this.map.put(uid, value);
	}

	public int toGridIndex(final Vector3f v)
	{
		return toGridIndex(v.x, v.y, v.z);
	}

	private final int getI(final float x)
	{
		return ((int) (x * multiplier) - this.minX) / cellSize;
	}

	private final int getJ(final float y)
	{
		return ((int) (y * multiplier) - this.minY) / cellSize;
	}

	private final int getK(final float z)
	{
		return ((int) (z * multiplier) - this.minZ) / cellSize;
	}

	public final int toGridIndex(final float x, final float y, final float z)
	{
		/**
		 * @formatter:off
		 */
		final int i = getI(x);
		final int j = getJ(y);
		final int k = getK(z);
		return getAbsoluteGridIndex(i,j,k);
		/**
		 * @formatter:on
		 */
	}

	private int getAbsoluteGridIndex(final int i, final int j, final int k)
	{
		return i + j * this.i_length + k * this.i_length * this.j_length;
	}

	public final Vector3f fromGridIndex(final int i, final Vector3f store)
	{
		final int grid_x = i % this.i_length;
		final int grid_y = i / i_length % j_length;
		final int grid_z = i / (i_length * j_length);

		final float x = grid_x * cellSize + minX;
		final float y = grid_y * cellSize + minY;
		final float z = grid_z * cellSize + minZ;

		return store.set(x, y, z).divideLocal(multiplier);

	}

	private static final ThreadLocal<Vector3f> forEach_storeVec_TL = ThreadLocal
			.withInitial(Vector3f::new);

	public void forEach(final BiConsumer<Vector3f, T> action)
	{
		final Vector3f storeVec = forEach_storeVec_TL.get();
		this.map.forEach((idx, value) -> {
			this.fromGridIndex(idx, storeVec);
			if (!Vector3f.isValidVector(storeVec))
			{
				log.error("Vector is not a valid vector");
			}
			action.accept(storeVec, value);
		});
	}

	public Map<Integer, T> getMap()
	{
		return map;
	}

	public void putAbsolute(final int pos, final T value)
	{
		this.map.put(pos, value);

	}

	public T getAbsolute(final int pos)
	{
		return this.map.get(pos);
	}

}
