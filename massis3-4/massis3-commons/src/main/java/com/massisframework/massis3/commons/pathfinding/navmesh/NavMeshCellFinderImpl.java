package com.massisframework.massis3.commons.pathfinding.navmesh;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import com.jme3.ai.navmesh.ICell;
import com.jme3.ai.navmesh.Line2D;
import com.jme3.bounding.BoundingBox;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.util.TempVars;
import com.massisframework.massis3.commons.spatials.TriOctree;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class NavMeshCellFinderImpl implements NavMeshCellFinder {

	/**
	 * TODO check
	 * https://web.archive.org/web/20161028140344/https://graphics.stanford.edu/~mdfisher/Code/Engine/Distance.cpp.html
	 */

	private final NavigationMesh<ICell> nm;
	private TriOctree<ICell> cellTree;

	public NavMeshCellFinderImpl(final NavigationMesh<ICell> nm)
	{
		this.nm = nm;

	}

	private void ensureTreeCreated()
	{
		if (this.cellTree == null)
		{
			BoundingBox bb = createBBFromNM(this.nm);

			this.cellTree = new TriOctree<>(bb, icell -> icell.getCenter());
			for (int i = 0; i < nm.getNumCells(); i++)
			{
				ICell cell = nm.getCell(i);
				this.cellTree.add(cell);
			}
		}
	}

	private static BoundingBox createBBFromNM(
			final NavigationMesh<ICell> navigationMesh)
	{
		final Vector3f min = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE,
				Float.MAX_VALUE);
		final Vector3f max = new Vector3f(Float.MIN_VALUE, Float.MIN_VALUE,
				Float.MIN_VALUE);
		for (int i = 0; i < navigationMesh.getNumCells(); i++)
		{
			final ICell cell = navigationMesh.getCell(i);
			for (final Vector3f pt : cell.getTriangle())
			{
				min.x = Math.min(pt.x, min.x);
				min.y = Math.min(pt.y, min.y);
				min.z = Math.min(pt.z, min.z);

				max.x = Math.max(pt.x, max.x);
				max.y = Math.max(pt.y, max.y);
				max.z = Math.max(pt.z, max.z);
			}
		}
		max.addLocal(0.1f, 0.1f, 0.1f);
		min.subtractLocal(0.1f, 0.1f, 0.1f);
		return new BoundingBox(min, max);
	}

	//
	@Deprecated
	public ICell findNearestCell_oct(Vector3f point)
	{
		ensureTreeCreated();
		List<ICell> neigh = this.cellTree.nodeNeighbors(point);
		if (neigh.isEmpty())
		{
			return findNearestCell(point);
		} else
		{
			return findNearestCell(point, neigh.get(0));
		}
	}

	/* (non-Javadoc)
	 * @see com.massisframework.massis3.commons.pathfinding.navmesh.NavMeshCellFinder#findNearestCell(com.jme3.math.Vector3f)
	 */
	@Override
	public ICell findNearestCell(final Vector3f point)
	{
		return findNearestCell(point, null);
	}

	@SuppressWarnings("unused")
	private ICell nearest(final Vector3f point, final ICell... cells)
	{
		float closestHeight = Float.POSITIVE_INFINITY;
		ICell closestCell = null;
		final Line2D motionPath = new Line2D(new Vector2f(), new Vector2f());
		final Vector2f intersection = new Vector2f();
		final Vector3f closestPoint3D = new Vector3f();
		float closestDistance = 3.4E+38f;
		float thisDistance;
		for (int i = 0; i < cells.length; i++)
		{
			final ICell cell = cells[i];
			if (cell == null)
				continue;
			if (cells[i] != null && cells[i].contains(point))
			{
				thisDistance = Math
						.abs(cells[i].getHeightOnCell(point) - point.y);
				if (thisDistance < closestHeight)
				{
					closestCell = cells[i];
					closestHeight = thisDistance;
				}
			}
			motionPath.setPoints(cell.getCenter().x, cell.getCenter().z,
					point.x, point.z);

			// ClassifyResult Result = cell.classifyPathToCell(motionPath);

			final boolean exiting = classifyPathToCell(cell, motionPath,
					intersection);

			if (exiting)
			{
				closestPoint3D.set(intersection.x, 0.0f, intersection.y);
				cell.computeHeightOnCell(closestPoint3D);

				closestPoint3D.subtractLocal(point);

				thisDistance = closestPoint3D.length();

				if (thisDistance < closestDistance)
				{
					closestDistance = thisDistance;
					closestCell = cell;
				}
			}
		}
		return closestCell;
	}

	private final ThreadLocal<IntSet> bfsSearch_visitedTL = ThreadLocal
			.withInitial(IntOpenHashSet::new);
	private final ThreadLocal<Queue<ICell>> bfsSearch_queueTL = ThreadLocal
			.withInitial(ArrayDeque::new);

	private ICell bfsSearch(final ICell lastHint, final Vector3f point)
	{
		final Queue<ICell> queue = bfsSearch_queueTL.get();
		queue.clear();
		queue.add(lastHint);

		final IntSet visited = bfsSearch_visitedTL.get();
		visited.clear();
		float minDist = Float.MAX_VALUE;
		ICell result = null;
		while (!queue.isEmpty())
		{
			final ICell current = queue.poll();
			visited.add(current.getIndex());
			if (current.contains(point))
			{
				final float dist = current.getCenter().distance(point);
				if (dist < minDist)
				{
					minDist = dist;
					result = current;
				}
			} else
			{
				for (int i = 0; i < 3; i++)
				{

					final ICell neigh = current.getLink(i);
					if (neigh != null && !visited.contains(neigh.getIndex()))
					{
						queue.add(neigh);
					}
				}
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see com.massisframework.massis3.commons.pathfinding.navmesh.NavMeshCellFinder#findNearestCell(com.jme3.math.Vector3f, com.jme3.ai.navmesh.ICell)
	 */
	@Override
	public ICell findNearestCell(final Vector3f point, final ICell lastHint)
	{

		// ICell cpnm = closesCellOnNavMesh(point);
		// if (cpnm != null)
		// {
		// return cpnm;
		// }
		// System.out.println("No closest point on navmesh!");

		if (lastHint != null
		// &&(lastHint.getCenter().getY() < point.getY() ||
		// FastMath.approximateEquals(lastHint.getCenter().getY(),point.getY()))
		)
		{

			if (lastHint.contains(point))
				return lastHint;
			for (int i = 0; i < 3; i++)
			{

				if (lastHint.getLink(i) != null
						&& lastHint.getLink(i).contains(point))
					return lastHint.getLink(i);
			}

			final ICell nearestC = bfsSearch(lastHint, point);
			if (nearestC != null)
			{
				return nearestC;
			}
			// ICell c = nearest(point, lastHint, lastHint.getLink(0),
			// lastHint.getLink(1), lastHint.getLink(2));
			// if (c != null)
			// return c;

		}
		// IntList store = new IntArrayList();
		// this.tree.getNearestTriangleIds(point, store);

		float closestDistance = 3.4E+38f;
		float closestHeight = 3.4E+38f;
		boolean foundHomeCell = false;
		float thisDistance;
		ICell closestCell = null;
		final Line2D motionPath = new Line2D(new Vector2f(), new Vector2f());
		final Vector3f closestPoint3D = new Vector3f();
		final Vector2f intersection = new Vector2f();

		// del store, sacamos las celdas
		// for (int i = 0; i < store.size(); i++)
		for (int i = 0; i < this.nm.getNumCells(); i++)
		{
			// ICell cell = cellMap.get(store.get(i));
			final ICell cell = this.nm.getCell(i);
			if (cell.contains(point))
			{
				thisDistance = Math.abs(cell.getHeightOnCell(point) - point.y);

				if (foundHomeCell)
				{
					if (thisDistance < closestHeight)
					{
						closestCell = cell;
						closestHeight = thisDistance;
					}
				} else
				{
					closestCell = cell;
					closestHeight = thisDistance;
					foundHomeCell = true;
				}
			}

			if (!foundHomeCell)
			{
				motionPath.setPoints(cell.getCenter().x, cell.getCenter().z,
						point.x, point.z);

				// ClassifyResult Result = cell.classifyPathToCell(motionPath);

				final boolean exiting = classifyPathToCell(cell, motionPath,
						intersection);

				if (exiting)
				{
					closestPoint3D.set(intersection.x, 0.0f, intersection.y);
					cell.computeHeightOnCell(closestPoint3D);

					closestPoint3D.subtractLocal(point);

					thisDistance = closestPoint3D.length();

					if (thisDistance < closestDistance)
					{
						closestDistance = thisDistance;
						closestCell = cell;
					}
				}
			}
		}

		return closestCell;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.jme3.ai.navmesh.ICell#classifyPathToCell(com.jme3.ai.navmesh.Line2D)
	 */
	private boolean classifyPathToCell(final ICell _this,
			final Line2D motionPath,
			final Vector2f store)
	{
		// System.out.println("Cell:"+m_Vertex[0].toString()+"
		// "+m_Vertex[1].toString()+" "+m_Vertex[2].toString());
		// System.out.println(" Path:"+MotionPath);
		int interiorCount = 0;
		store.set(0, 0);
		// ClassifyResult result = new ClassifyResult();

		// Check our MotionPath against each of the three cell walls
		for (int i = 0; i < 3; ++i)
		{
			// Classify the MotionPath endpoints as being either ON_LINE,
			// or to its LEFT_SIDE or RIGHT_SIDE.
			// Since our triangle vertices are in clockwise order,
			// we know that points to the right of each line are inside the
			// cell.
			// Points to the left are outside.
			// We do this test using the ClassifyPoint function of Line2D

			// If the destination endpoint of the MotionPath
			// is Not on the right side of this wall...
			final Line2D.PointSide end = _this.getWall(i).getSide(
					motionPath.getPointB(), 0.0f);
			if (end == Line2D.PointSide.Left)
			{// (end != Line2D.PointSide.Right) {
				// && end != Line2D.POINT_CLASSIFICATION.ON_LINE) {
				// ..and the starting endpoint of the MotionPath
				// is Not on the left side of this wall...
				if (_this.getWall(i).getSide(motionPath.getPointA(),
						0.0f) != Line2D.PointSide.Left)
				{
					// Check to see if we intersect the wall
					// using the Intersection function of Line2D
					final Line2D.LineIntersect IntersectResult = motionPath
							.intersect(_this.getWall(i), store);

					if (IntersectResult == Line2D.LineIntersect.SegmentsIntersect
							|| IntersectResult == Line2D.LineIntersect.ABisectsB)
					{
						// record the link to the next adjacent cell
						// (or NULL if no attachement exists)
						// and the enumerated ID of the side we hit.
						// result.cell = _this.getLink(i);//links[i];
						// result.side = i;
						// result.result = PathResult.ExitingCell;
						// System.out.println("exits this cell");
						// return result;
						return true;

						// pNextCell = m_Link[i];
						// Side = i;
						// return (PATH_RESULT.EXITING_CELL);
					}
				}
			} else
			{
				// The destination endpoint of the MotionPath is on the right
				// side.
				// Increment our InteriorCount so we'll know how many walls we
				// were
				// to the right of.
				interiorCount++;
			}
		}

		// An InteriorCount of 3 means the destination endpoint of the
		// MotionPath
		// was on the right side of all walls in the cell.
		// That means it is located within this triangle, and this is our ending
		// cell.
		if (interiorCount == 3)
		{
			// System.out.println(" ends within this cell");
			// result.result = PathResult.EndingCell;
			// return result;
			return false;
			// return (PATH_RESULT.ENDING_CELL);
		}
		// System.out.println("No intersection with this cell at all");
		// We only reach here is if the MotionPath does not intersect the cell
		// at all.
		// return result;
		return false;
		// return (PATH_RESULT.NO_RELATIONSHIP);
	}

	// private static class ICellBB implements BoundingBoxContainable {
	//
	// private ICell cell;
	//
	// public ICellBB(ICell cell)
	// {
	// this.cell = cell;
	// }
	//
	// @Override
	// public boolean isContainedBy(BoundingBox bb)
	// {
	// for (Vector3f pt : cell.getTriangle())
	// {
	// if (!bb.contains(pt))
	// return false;
	// }
	// return true;
	// }
	//
	// @Override
	// public boolean intersectsWith(BoundingBox bb)
	// {
	// Vector3f[] tri = cell.getTriangle();
	// return isContainedBy(bb) || bb.intersects(tri[0], tri[1], tri[2]);
	// }
	//
	// @Override
	// public Vector3f nearestPointTo(Vector3f other)
	// {
	// return closesPointOnTriangle(this.cell.getTriangle(), other);
	// }
	//
	//
	//
	// public ICell getCell()
	// {
	// return cell;
	// }
	//
	// }
	public static Vector3f closesPointOnTriangle(final Vector3f[] triangle,
			final Vector3f sourcePosition)
	{

		final TempVars tmp = TempVars.get();
		final Vector3f edge0 = triangle[1].subtract(triangle[0], tmp.vect1);
		final Vector3f edge1 = triangle[2].subtract(triangle[0], tmp.vect2);
		final Vector3f v0 = triangle[0].subtract(sourcePosition, tmp.vect3);

		final float a = edge0.dot(edge0);
		final float b = edge0.dot(edge1);
		final float c = edge1.dot(edge1);
		final float d = edge0.dot(v0);
		final float e = edge1.dot(v0);

		final float det = a * c - b * b;
		float s = b * e - c * d;
		float t = b * d - a * e;

		if (s + t < det)
		{
			if (s < 0.f)
			{
				if (t < 0.f)
				{
					if (d < 0.f)
					{
						s = FastMath.clamp(-d / a, 0.f, 1.f);
						t = 0.f;
					} else
					{
						s = 0.f;
						t = FastMath.clamp(-e / c, 0.f, 1.f);
					}
				} else
				{
					s = 0.f;
					t = FastMath.clamp(-e / c, 0.f, 1.f);
				}
			} else if (t < 0.f)
			{
				s = FastMath.clamp(-d / a, 0.f, 1.f);
				t = 0.f;
			} else
			{
				final float invDet = 1.f / det;
				s *= invDet;
				t *= invDet;
			}
		} else
		{
			if (s < 0.f)
			{
				final float tmp0 = b + d;
				final float tmp1 = c + e;
				if (tmp1 > tmp0)
				{
					final float numer = tmp1 - tmp0;
					final float denom = a - 2 * b + c;
					s = FastMath.clamp(numer / denom, 0.f, 1.f);
					t = 1 - s;
				} else
				{
					t = FastMath.clamp(-e / c, 0.f, 1.f);
					s = 0.f;
				}
			} else if (t < 0.f)
			{
				if (a + d > b + e)
				{
					final float numer = c + e - b - d;
					final float denom = a - 2 * b + c;
					s = FastMath.clamp(numer / denom, 0.f, 1.f);
					t = 1 - s;
				} else
				{
					s = FastMath.clamp(-e / c, 0.f, 1.f);
					t = 0.f;
				}
			} else
			{
				final float numer = c + e - b - d;
				final float denom = a - 2 * b + c;
				s = FastMath.clamp(numer / denom, 0.f, 1.f);
				t = 1.f - s;
			}
		}
		final Vector3f result = triangle[0].add(edge0.mult(s))
				.add(edge1.mult(t));
		tmp.release();
		return result;
	}

}
