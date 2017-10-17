package com.jme3.ai.navmesh.util;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

public class WallsMesh extends Mesh {

	private FloatList vertices;
	private IntList indices;
	int quadCount = 0;

	public WallsMesh(final int expected)
	{
		this.vertices = new FloatArrayList(expected * 12);
		this.indices = new IntArrayList(expected * 6);
	}

	public void addWall(final Vector3f start, final Vector3f end,
			final float height)
	{

		vertices.add(start.x);
		vertices.add(start.y);
		vertices.add(start.z);

		vertices.add(end.x);
		vertices.add(end.y);
		vertices.add(end.z);

		vertices.add(end.x);
		vertices.add(end.y + height);
		vertices.add(end.z);

		vertices.add(start.x);
		vertices.add(start.y + height);
		vertices.add(start.z);

		indices.add(quadCount * 4 + 0);
		indices.add(quadCount * 4 + 1);
		indices.add(quadCount * 4 + 2);
		indices.add(quadCount * 4 + 0);
		indices.add(quadCount * 4 + 2);
		indices.add(quadCount * 4 + 3);

		quadCount += 1;

	}

	public void build()
	{
		final FloatBuffer positionsBuff = BufferUtils
				.createFloatBuffer(vertices.size());
		for (final float v : vertices)
		{
			positionsBuff.put(v);
		}
		final IntBuffer indexBuff = BufferUtils.createIntBuffer(indices.size());
		for (final int v : indices)
		{
			indexBuff.put(v);
		}
		this.setBuffer(Type.Position, 3, positionsBuff);
		this.setBuffer(Type.Index, 3, indexBuff);

		this.updateCounts();
		this.updateBound();
	}

	public void clear()
	{
		this.indices = null;
		this.vertices = null;
	}

}
