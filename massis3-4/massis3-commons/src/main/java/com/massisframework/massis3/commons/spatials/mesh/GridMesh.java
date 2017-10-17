package com.massisframework.massis3.commons.spatials.mesh;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.math.ColorRGBA;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;

public class GridMesh extends Mesh {

	private static final Logger logger = LoggerFactory
			.getLogger(GridMesh.class);
	public final float maxZ;
	public final float maxX;
	public final float minZ;
	public final float minX;
	public final float cellSize;
	public final float xlength;
	public final float zlength;
	public final int xCells;
	public final int zCells;
	public final float elevation;

	private float maxValue = .1f;
	private float minValue = .1f;
	private final float[] grid;

	public GridMesh(
			final float cellSize,
			final float minX,
			final float minZ,
			final float maxX,
			final float maxZ,
			final float elevation)
	{
		this.cellSize = cellSize;
		this.minX = minX;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxZ = maxZ;
		this.xlength = this.maxX - this.minX;
		this.zlength = this.maxZ - this.minZ;
		this.xCells = (int) (this.xlength / this.cellSize) + 1;
		this.zCells = (int) (this.zlength / this.cellSize) + 1;
		this.elevation = elevation;
		this.grid = new float[this.xCells * this.zCells];
		int neededSize = 0;
		for (float x = this.minX; x < this.maxX; x += this.cellSize)
		{
			for (float z = this.minZ; z < this.maxZ; z += this.cellSize)
			{
				neededSize++;
			}
		}
		if (logger.isInfoEnabled())
		{
			logger.info(
					"Parameters: xCells: {}. zCells: {}. Size {}. Needed: {}",
					this.xCells, this.zCells,
					this.xCells * this.zCells, neededSize);
		}

		this.build();
	}

	private void build()
	{
		// Index buffer > all positions.
		final int positionsBufferSize = this.xCells * this.zCells * 3;
		final FloatBuffer positionsBuffer = BufferUtils
				.createFloatBuffer(positionsBufferSize);
		this.setBuffer(Type.Position, 3, positionsBuffer);
		final int indexBufferSize = this.xCells * this.zCells * 6;
		final IntBuffer indexBuffer = BufferUtils
				.createIntBuffer(indexBufferSize);
		this.setBuffer(Type.Index, 3, indexBuffer);

		for (float x = this.minX; x < this.maxX; x += this.cellSize)
		{
			for (float z = this.minZ; z < this.maxZ; z += this.cellSize)
			{
				positionsBuffer.put(x);
				positionsBuffer.put(this.elevation);
				positionsBuffer.put(z);
			}
		}

		for (int i = 0; i < this.xCells - 1; i++)
		{
			for (int j = 0; j < this.zCells - 1; j++)
			{

				/**
				 * <pre>
					2\2--3
					| \  | Counter-clockwise
					|  \ |
					0--1\1
					
					0:  (i+1,j)
					1:  (i+1,j+1)
					2 : (i,j)
					3:  (i,j+1)
					
					indexes={ (i,j), (i+1,j), (i+1,j+1), (i+1,j+1), (i,j+1), (i,j) }
				 * 
				 * </pre>
				 */
				indexBuffer

						.put(getAbsolutePos(i + 1, j + 1))
						.put(getAbsolutePos(i + 1, j))
						.put(getAbsolutePos(i, j))

						.put(getAbsolutePos(i, j + 1))
						.put(getAbsolutePos(i + 1, j + 1))
						.put(getAbsolutePos(i, j));

			}
		}

	}

	private int getAbsolutePos(final int i, final int j)
	{
		return i * this.zCells + j;
	}

	private int getCell(final float x, final float z)
	{
		final int i = getI(x, z);
		final int j = getJ(x, z);
		return getAbsolutePos(i, j);
	}

	private int getI(final float x, final float z)
	{
		return (int) ((x - this.minX) / this.cellSize);
	}

	private int getJ(final float x, final float z)
	{
		return (int) ((z - this.minZ) / this.cellSize);
	}

	public void incValue(final float x, final float z, final float value)
	{

		final int index = getCell(x, z);
		if (index >= this.grid.length || index < 0 || value < 0)
		{
			return;
		}

		if (this.grid[index] + value > 0)
		{
			this.grid[index] += value;
			this.minValue = this.grid[index] < this.minValue ? this.grid[index] : this.minValue;
			this.maxValue = this.grid[index] > this.maxValue ? this.grid[index] : this.maxValue;

		}

	}

	public void reset()
	{
		Arrays.fill(this.grid, 0f);
		this.minValue = 0.01f;
		this.maxValue = 0.01f;
	}

	public float getValue(final float x, final float z)
	{
		return this.grid[getCell(x, z)];
	}

	public void dumpHeatMap()
	{

		if (this.getBuffer(Type.Color) == null)
		{
			this.setBuffer(Type.Color, 4,
					BufferUtils.createFloatBuffer(this.getVertexCount() * 4));
		}
		// TODO ojo NO thread safe
		// Tenemos el maximo y el minimo.
		final VertexBuffer colorBuffer = this.getBuffer(Type.Color);
		final FloatBuffer dataBuffer = (FloatBuffer) colorBuffer.getData();
		final ColorRGBA store = new ColorRGBA();
		for (int i = 0; i < this.xCells; i++)
		{
			for (int j = 0; j < this.zCells; j++)
			{
				final int index = getAbsolutePos(i, j);
				final float val = this.grid[index];
				final double percent = getNormalized(val);
				// store.interpolate(ColorRGBA.Red, ColorRGBA.Green,
				// (float) percent);
				final int rgbint = Color.HSBtoRGB((float) percent, 1, 1);
				store.r = (rgbint >> 16 & 0xFF) / 255f;
				store.g = (rgbint >> 8 & 0xFF) / 255f;
				store.b = (rgbint >> 0 & 0xFF) / 255f;
				store.a = val <= this.minValue ? 0f : 1f;

				dataBuffer.put(index * 4 + 0, store.r);
				dataBuffer.put(index * 4 + 1, store.g);
				dataBuffer.put(index * 4 + 2, store.b);
				dataBuffer.put(index * 4 + 3, store.a);
			}
		}
		colorBuffer.setUpdateNeeded();
	}

	private double getNormalized(final float val)
	{
		final double norm_data = (1 - 1D * val / this.maxValue) * 0.7D;
		return norm_data;
	}

}