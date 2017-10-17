package com.massisframework.massis3.commons.spatials.mesh;

import java.nio.FloatBuffer;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;

/**
 * Circle.
 * 
 * @author Martin Simons
 * @version $Id$
 */
public class Circle3dMesh extends Mesh {

	/**
	 * The center.
	 */
	private final Vector3f center;
	/**
	 * The radius.
	 */
	private final float radius;
	/**
	 * The samples.
	 */
	private final int samples;

	/**
	 * Constructs a new instance of this class.
	 * 
	 * @param radius
	 */
	public Circle3dMesh(final float radius)
	{
		this(Vector3f.ZERO, radius, 16);
	}

	/**
	 * Constructs a new instance of this class.
	 * 
	 * @param radius
	 * @param samples
	 */
	public Circle3dMesh(final float radius, final int samples)
	{
		this(Vector3f.ZERO, radius, samples);
	}

	/**
	 * Constructs a new instance of this class.
	 * 
	 * @param center
	 * @param radius
	 * @param samples
	 */
	public Circle3dMesh(final Vector3f center, final float radius,
			final int samples)
	{
		super();
		this.center = center;
		this.radius = radius;
		this.samples = samples;

		setMode(Mode.Lines);
		updateGeometry();
	}

	protected void updateGeometry()
	{
		final FloatBuffer positions = BufferUtils
				.createFloatBuffer(samples * 3);
		final FloatBuffer normals = BufferUtils.createFloatBuffer(samples * 3);
		final short[] indices = new short[samples * 2];

		final float rate = FastMath.TWO_PI / samples;
		float angle = 0;
		int idc = 0;
		for (int i = 0; i < samples; i++)
		{
			final float x = FastMath.cos(angle) * radius + center.x;
			final float z = FastMath.sin(angle) * radius + center.z;

			positions.put(x).put(center.y).put(z);
			normals.put(new float[] { 0, 1, 0 });

			indices[idc++] = (short) i;
			if (i < samples - 1)
			{
				indices[idc++] = (short) (i + 1);
			} else
			{
				indices[idc++] = 0;
			}

			angle += rate;
		}

		setBuffer(Type.Position, 3, positions);
		setBuffer(Type.Normal, 3, normals);
		setBuffer(Type.Index, 2, indices);

		setBuffer(Type.TexCoord, 2, new float[] { 0, 0, 1, 1 });

		updateBound();
	}
}
