package com.massisframework.massis3.commons.pathfinding.navmesh;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;

import com.jme3.ai.navmesh.ICell;
import com.jme3.math.Vector3f;
import com.jme3.util.TempVars;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public final class NavigationMeshUtil {

	private NavigationMeshUtil()
	{
	}

	public static Vector3f commonMidPoint(final ICell cell1, final ICell cell2)
	{
		for (int i = 0; i < cell1.getNumLinks(); i++)
			if (cell1.getLink(i) != null)
				if (cell1.getLink(i) == cell2)
					return cell1.getWallMidpoint(i).clone();
		return null;
	}

	private static int commonIndexFromFirstToSecond(final ICell cell1,
			final ICell cell2)
	{
		for (int i = 0; i < 3; i++)
		{
			if (cell1.getLink(i) != null)
			{
				if (cell1.getLink(i) == cell2)
				{
					return i;
				}
			}
		}
		return -1;
	}

	public static int[] commonIndexes(final ICell cell1, final ICell cell2,
			int[] store)
	{
		if (store == null)
		{
			store = new int[2];
		}
		store[0] = commonIndexFromFirstToSecond(cell1, cell2);
		store[1] = commonIndexFromFirstToSecond(cell2, cell1);
		return store;
	}

	public static Vector3f[] commonWall(final ICell cell1, final ICell cell2)
	{
		for (int i = 0; i < 3; i++)
			if (cell1.getLink(i) != null)
				if (cell1.getLink(i) == cell2)
					return new Vector3f[] { cell1.getVertex(i).clone(),
							cell1.getVertex((i + 1) % 3).clone() };
		return null;
	}

	private static ThreadLocal<Queue<ICell>> nearestObstaclePoint_queue_TL = ThreadLocal
			.withInitial(ArrayDeque::new);
	private static ThreadLocal<IntSet> nearestObstaclePoint_visited_TL = ThreadLocal
			.withInitial(IntOpenHashSet::new);

	public static Vector3f nearestObstaclePoint(final Vector3f p,
			final ICell hint)
	{
		final TempVars tmp = TempVars.get();
		final Queue<ICell> queue = nearestObstaclePoint_queue_TL.get();
		queue.clear();
		final IntSet visited = nearestObstaclePoint_visited_TL.get();
		visited.clear();
		float minDist = Float.MAX_VALUE;
		queue.add(hint);
		final Vector3f closest = new Vector3f();
		final Vector3f closest2 = new Vector3f();
		while (!queue.isEmpty())
		{
			final ICell cell = queue.poll();
			if (visited.contains(cell.getIndex()))
			{
				continue;
			}
			visited.add(cell.getIndex());

			for (int i = 0; i < cell.getNumLinks(); i++)
			{
				final Vector3f a = cell.getVertex(i);
				final Vector3f b = cell.getVertex((i + 1) % cell.getNumLinks());
				if (cell.getLink(i) == null)
				{
					closestPointOnLineSegment(a, b, p, closest2);
					final float dist = closest2.distance(p);
					if (dist < minDist)
					{
						minDist = dist;
						closest.set(closest2);
					}
				} else
				{
					for (int j = 0; j < cell.getNumLinks(); j++)
					{
						final Vector3f link_a = cell.getLink(i).getVertex(j);
						final Vector3f link_b = cell.getLink(i)
								.getVertex((j + 1) % cell.getNumLinks());
						if (closestPointOnLineSegment(link_a, link_b, p,
								tmp.vect1).distance(p) <= minDist)
						{
							queue.add(cell.getLink(i));
							break;
						}
					}

				}
			}
		}
		tmp.release();
		return closest;
	}

	/**
	 * https://web.archive.org/save/_embed/http://stackoverflow.com/questions/
	 * 3120357/get-closest-point-to-a-line/9557244#9557244
	 * 
	 */
	private static Vector3f closestPointOnLineSegment(final Vector3f A,
			final Vector3f B,
			final Vector3f P, Vector3f result)
	{
		if (result == null)
			result = new Vector3f();
		else
			result.set(Vector3f.ZERO);
		final TempVars tmp = TempVars.get();
		final Vector3f AP = P.subtract(A, tmp.vect1); // Vector from A to P
		final Vector3f AB = B.subtract(A, tmp.vect2); // Vector from A to B

		final float magnitudeAB = AB.lengthSquared(); // Magnitude of AB vector
														// (it's
		// length squared)
		final float ABAPproduct = AP.dot(AB); // The DOT product of a_to_p and
												// a_to_b
		final float distance = ABAPproduct / magnitudeAB; // The normalized
															// "distance"
		// from a to your closest
		// point

		if (distance < 0) // Check if P projection is over vectorAB
		{
			result.set(A);

		} else if (distance > 1)
		{
			result.set(B);
		} else
		{
			result.set(A.add(AB.mult(distance, tmp.vect3), tmp.vect4));
		}
		tmp.release();
		return result;
	}

	public static List<Vector3f> toMidPointPath(
			final List<Integer> cellPath,
			final List<Vector3f> store,
			final Function<Integer, ICell> transformFn)
	{

		store.clear();
		for (int i = 0; i < cellPath.size() - 1; i++)
		{
			store.add(
					commonMidPoint(
							transformFn.apply(cellPath.get(i)),
							transformFn.apply(cellPath.get(i + 1)))
									.clone());
		}
		return store;
	}

	public static void toMidPointPath(
			final List<ICell> cellPath,
			final List<Vector3f> store)
	{

		store.clear();
		for (int i = 0; i < cellPath.size() - 1; i++)
		{
			store.add(commonMidPoint(cellPath.get(i), cellPath.get(i + 1))
					.clone());
		}
	}

	public static void toWallsPath(
			final List<Integer> cellPath,
			final List<Vector3f[]> store,
			final Function<Integer, ICell> transformFn)
	{

		store.clear();
		for (int i = 0; i < cellPath.size() - 1; i++)
		{
			store.add(commonWall(
					transformFn.apply(cellPath.get(i)),
					transformFn.apply(cellPath.get(i + 1))));
			// store.add(commonMidPoint(cellPath.get(i), cellPath.get(i + 1))
			// .clone());
			// find common link

		}
	}

	public static void toWallsPath(final List<ICell> cellPath,
			final List<Vector3f[]> store)
	{

		store.clear();
		for (int i = 0; i < cellPath.size() - 1; i++)
		{
			store.add(commonWall(cellPath.get(i), cellPath.get(i + 1)));
			// store.add(commonMidPoint(cellPath.get(i), cellPath.get(i + 1))
			// .clone());
			// find common link

		}
	}

}
