package com.massisframework.massis3.commons.loader;

import java.util.Queue;

import com.jme3.bounding.BoundingVolume;
import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResults;
import com.jme3.collision.UnsupportedCollisionException;
import com.jme3.scene.Geometry;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;
import com.jme3.util.SafeArrayList;

public class BuildingSpatial extends Spatial {

	private final SafeArrayList<Geometry> geoms = new SafeArrayList<>(
			Geometry.class);

	@Override
	public int collideWith(final Collidable other,
			final CollisionResults results)
			throws UnsupportedCollisionException
	{
		int total = 0;
		for (final Spatial child : geoms.getArray())
		{
			total += child.collideWith(other, results);
		}
		return total;
	}

	@Override
	public void updateModelBound()
	{

	}

	@Override
	public void setModelBound(final BoundingVolume modelBound)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public int getVertexCount()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getTriangleCount()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void depthFirstTraversal(final SceneGraphVisitor visitor)
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void breadthFirstTraversal(final SceneGraphVisitor visitor,
			final Queue<Spatial> queue)
	{
		// TODO Auto-generated method stub

	}

}
