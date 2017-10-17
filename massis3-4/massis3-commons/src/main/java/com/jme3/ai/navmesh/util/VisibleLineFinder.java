package com.jme3.ai.navmesh.util;

import java.util.List;

import com.jme3.ai.navmesh.ICell;
import com.jme3.ai.navmesh.Line2D;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.util.TempVars;
import com.massisframework.massis3.commons.pathfinding.navmesh.NavigationMesh;

public class VisibleLineFinder {

	private final NavigationMesh<ICell> navigationMesh;
	private WallsMesh mesh;
	private final Geometry meshWrapper;

	public VisibleLineFinder(final NavigationMesh<ICell> navigationMesh)
	{
		this.navigationMesh = navigationMesh;
		this.buildWallsMesh();
		this.meshWrapper = new Geometry("VisibleLineFinderGeom", this.mesh);

	}

	private void buildWallsMesh()
	{
		final TempVars tmp = TempVars.get();
		final int nCells = this.navigationMesh.getNumCells();
		final int nwalls = calcNWalls();

		this.mesh = new WallsMesh(nwalls);
		for (int i = 0; i < nCells; i++)
		{
			final ICell c = this.navigationMesh.getCell(i);
			for (int k = 0; k < 3; k++)
			{
				if (c.getLink(k) == null)
				{

					final Line2D wall = c.getWall(k);
					final Vector2f a = wall.getPointA();
					final Vector2f b = wall.getPointB();
					final float y = c.getCenter().y - 0.1f;
					final float height = 5;
					final Vector3f end = tmp.vect1.set(a.x, y, a.y);
					final Vector3f start = tmp.vect2.set(b.x, y, b.y);
					this.mesh.addWall(start, end, height);

				}
			}
		}
		this.mesh.build();
		this.mesh.clear();
		tmp.release();
	}

	private static ThreadLocal<Ray> intersectsWithRayTL = ThreadLocal
			.withInitial(Ray::new);

	public int visibleLine(final List<ICell> cells)
	{
		if (cells.size() < 2)
		{
			return 0;
		}
		final Vector3f start = cells.get(0).getCenter();

		for (int i = 1; i < cells.size(); i++)
		{
			final Vector3f end = cells.get(i).getCenter();
			final boolean intersection = intersectsWith(start, end);
			if (intersection)
			{
				return i;
			}
		}
		return cells.size() - 1;
	}

	private boolean intersectsWith(final Vector3f a, final Vector3f b)
	{
		final Ray ray = intersectsWithRayTL.get();
		final TempVars tmp = TempVars.get();
		final Vector3f origin = tmp.vect1.set(a);
		final Vector3f sub = b.subtract(a, tmp.vect2);

		final Vector3f direction = tmp.vect3.set(sub).normalizeLocal();

		final float limit = sub.length();
		ray.setOrigin(origin);
		ray.setDirection(direction);
		ray.setLimit(limit);

		final CollisionResults crs = tmp.collisionResults;
		crs.clear();
		final int nColl = this.meshWrapper.collideWith(ray, crs);
		crs.clear();
		tmp.release();
		return nColl > 0;
	}

	private int calcNWalls()
	{
		int nwalls = 0;
		final int nCells = this.navigationMesh.getNumCells();
		for (int i = 0; i < nCells; i++)
		{
			for (int k = 0; k < 3; k++)
			{
				if (this.navigationMesh.getCell(i).getLink(k) == null)
				{
					nwalls++;
				}
			}
		}
		return nwalls;
	}
}
