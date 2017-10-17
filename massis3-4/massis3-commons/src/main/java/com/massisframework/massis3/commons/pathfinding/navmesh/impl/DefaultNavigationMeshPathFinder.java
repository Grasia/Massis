package com.massisframework.massis3.commons.pathfinding.navmesh.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.jme3.math.Vector3f;
import com.massisframework.massis3.commons.pathfinding.navmesh.FindPathResult;
import com.massisframework.massis3.commons.pathfinding.navmesh.NavMeshNode;
import com.massisframework.massis3.commons.pathfinding.navmesh.NavigationMesh;
import com.massisframework.massis3.commons.pathfinding.navmesh.NavigationMeshPathFinder;

import it.unimi.dsi.fastutil.ints.AbstractIntPriorityQueue;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntHeapPriorityQueue;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public class DefaultNavigationMeshPathFinder<NMNode extends NavMeshNode<NMNode>>
		implements NavigationMeshPathFinder<NMNode> {

	private static final int CACHE_MAX_SIZE = 1024;
	private static final int CELL_CACHE_MAX_CAPACITY = 1024;
	private NavigationMesh<NMNode> navMesh;

	private int[][] adjList;
	private float[][] adjDistances;

	private BiMap<NMNode, Integer> cellIdMap;
	private ICellCache cache;

	public DefaultNavigationMeshPathFinder(NavigationMesh<NMNode> nm)
	{
		this.navMesh = nm;
		this.build();
	}

	@Override
	public FindPathResult findPath(Vector3f start, Vector3f goal,
			int pathMaxLength, List<NMNode> store)
	{
		return this.findPath(findCell(start), findCell(goal), pathMaxLength,
				store);
	}

	// ========================================================================

	/**
	 * Only use with {@link #findPath(ICell, ICell, int, List)}
	 */
	private static ThreadLocal<IntList> findPath_path_TL = ThreadLocal
			.withInitial(IntArrayList::new);

	@Override
	public FindPathResult findPath(
			NMNode cellA,
			NMNode cellB,
			int pathMaxLength,
			List<NMNode> store)
	{
		store.clear();

		IntList path = findPath_path_TL.get();
		path.clear();

		int found = 0;
		List<NMNode> test = new ArrayList<>();
		NMNode cellAA = cellA;
		NMNode next = getNextCellInPathTo(cellAA, cellB);
		FindPathResult result = null;
		while (next != null && cellAA != cellB && found <= pathMaxLength)
		{
			cellAA = next;
			NMNode next2 = getNextCellInPathTo(next, cellB);
			next = next2;
			found++;
			test.add(cellAA);
		}
		if (cellAA == cellB)
		{
			store.addAll(test);

			result = FindPathResult.COMPLETE_PATH_FOUND;
		} else if (found >= pathMaxLength)
		{
			store.addAll(test);

			result = FindPathResult.PARTIAL_PATH_FOUND;
		} else
		{
			result = findPath(cellA, cellB, path);

			if (result != FindPathResult.NOT_FOUND)
			{
				path.stream().map(id -> this.cellIdMap.inverse().get(id))
						.forEach(store::add);
				Collections.reverse(store);

				storeInCache(store);
			}

		}

		return result;
	}

	// ========================================================================
	private void storeInCache(List<NMNode> store)
	{
		for (int i = 0; i < store.size() - 1; i++)
		{
			NMNode c1 = store.get(i);
			NMNode c2 = store.get(i + 1);

			int c1_id = this.cellIdMap.get(c1);

			int linkId = getCommonLink(c1, c2);

			for (int i2 = i + 1; i2 < store.size(); i2++)
			{
				NMNode c3 = store.get(i2);
				int c3_id = this.cellIdMap.get(c3);
				this.cache.addWay(c1_id, c3_id, linkId);
			}
		}

	}

	private static int getCommonLink(NavMeshNode<?> c1, NavMeshNode<?> c2)
	{
		for (int j = 0; j < c1.getNumLinks(); j++)
		{
			if (c1.getLink(j) == c2)
			{
				return j;
			}
		}
		return -1;
	}

	public NMNode getNextCellInPathTo(NMNode cellA, NMNode cellB)
	{
		if (cellA == cellB)
		{
			return null;
		}
		int cellAID = this.cellIdMap.get(cellA);
		int cellBID = this.cellIdMap.get(cellB);

		int linkIndex = cache.getLinkIndex(cellAID, cellBID);

		if (linkIndex >= 0)
		{
			return cellA.getLink(linkIndex);
		}
		return null;

	}

	private void build()
	{

		/*
		 * TODO porque aqui y abajo? this.adjList = new
		 * int[navMesh.getNumCells()][];
		 */
		this.adjDistances = new float[navMesh.getNumCells()][];
		this.cache = new ICellCache(CACHE_MAX_SIZE, CELL_CACHE_MAX_CAPACITY);
		this.cellIdMap = HashBiMap.create(this.navMesh.getNumCells());
		int cell_counter = 0;
		for (int i = 0; i < navMesh.getNumCells(); i++)
		{
			NMNode c = navMesh.getCell(i);
			if (!this.cellIdMap.containsKey(c))
			{
				cellIdMap.put(c, cell_counter);
				cell_counter++;
			}

			for (int j = 0; j < c.getNumLinks(); j++)
			{
				final NMNode link = c.getLink(j);

				if (link != null)
				{
					if (!this.cellIdMap.containsKey(link))
					{
						cellIdMap.put(link, cell_counter);
						// cellMap.put(cell_counter, link);
						cell_counter++;
					}

				}
			}
		}
		this.adjList = new int[cell_counter][];
		this.adjDistances = new float[cell_counter][];

		for (int i = 0; i < cell_counter; i++)
		{
			final NMNode c = this.cellIdMap.inverse().get(i);
			int nlinks = 0;
			for (int j = 0; j < c.getNumLinks(); j++)
				if (c.getLink(j) != null)
					nlinks++;

			this.adjList[i] = new int[nlinks];
			this.adjDistances[i] = new float[nlinks];
			for (int j = 0, k = 0; j < c.getNumLinks(); j++)
			{
				final NMNode link = c.getLink(j);
				if (link != null)
				{
					this.adjList[i][k] = cellIdMap.get(link);
					this.adjDistances[i][k] = link.getCenter()
							.distance(c.getCenter());
					k++;
				}
			}
		}
	}

	private NMNode findCell(Vector3f p)
	{
		return this.navMesh.findClosestCell(p);
	}

	protected FindPathResult findPath(NMNode cellA, NMNode cellB, IntList store)
	{
		final int start = this.cellIdMap.get(cellA);
		final int goal = this.cellIdMap.get(cellB);
		AStarUtils utils = TL.get();
		utils.clear();
		/*
		 * Initialization
		 */
		// The set of nodes already evaluated.
		IntOpenHashSet closedSet = utils.closedSet;
		IntOpenHashSet openSet = utils.openSet;

		// The set of currently discovered nodes still to be evaluated.
		// Initially, only the start node is known.
		openSet.add(start);
		// For each node, which node it can most efficiently be reached from.
		// If a node can be reached from many nodes, cameFrom will eventually
		// contain the
		// most efficient previous step.
		Int2IntOpenHashMap cameFrom = utils.cameFrom;
		// For each node, the cost of getting from the start node to that node.
		Int2FloatOpenHashMap gScore = utils.gScore;
		// The cost of going from start to start is zero.
		gScore.put(start, 0);
		// For each node, the total cost of getting from the start node to the
		// goal
		// by passing by that node. That value is partly known, partly
		// heuristic.
		NodeDistPriorityQueue fScore = utils.fScore;
		// For the first node, that value is completely heuristic.
		fScore.add(start, heuristic_cost_estimate(start, goal));

		while (!openSet.isEmpty())
		{
			// final int current = 0;// := the node in openSet having the lowest
			// fScore[] value
			// AdjListCache adjListCache = adjListCacheTL.get();
			int current = fScore.poll();

			if (current == goal)
			{
				reconstruct_path(cameFrom, current, store);
				return FindPathResult.COMPLETE_PATH_FOUND;
			}
			// if (adjListCache.hasPartial(current, goal))
			// {
			// reconstruct_path(cameFrom, current, store);
			// adjListCache.fillPartialPath(current, goal, store);
			// return FindPathResult.PARTIAL;
			// }
			openSet.remove(current);
			closedSet.add(current);
			// for each neighbor of current
			for (int n = 0; n < this.adjList[current].length; n++)
			{
				final int neighbor = this.adjList[current][n];
				if (closedSet.contains(neighbor))
				{
					continue;// Ignore the neighbor which is already evaluated.
				}
				// The distance from start to a neighbor
				final float tentative_gScore = gScore.get(current)
						+ this.adjDistances[current][n];
				// tentative_gScore := gScore[current] + dist_between(current,
				// neighbor)
				if (!openSet.contains(neighbor))
				{
					openSet.add(neighbor);
				} else if (tentative_gScore >= gScore.get(neighbor))
				{
					continue;
				}
				cameFrom.put(neighbor, current);
				gScore.put(neighbor, tentative_gScore);
				fScore.add(neighbor, gScore.get(neighbor)
						+ heuristic_cost_estimate(neighbor, goal));
			}
		}

		return FindPathResult.NOT_FOUND;
	}

	private IntList reconstruct_path(Int2IntOpenHashMap cameFrom, int current,
			IntList store)
	{
		// store.clear(); -> No because we accumulate partial results here.
		IntList total_path = store;// new IntArrayList();
		total_path.add(current);
		while (cameFrom.containsKey(current))
		{
			current = cameFrom.get(current);
			total_path.add(current);
		}
		return total_path;
	}

	private final float heuristic_cost_estimate(int start, int goal)
	{
		final Vector3f startV = this.navMesh.getCell(start).getCenter();
		final Vector3f endV = this.navMesh.getCell(goal).getCenter();
		return startV.distance(endV);
	}

	private final ThreadLocal<AStarUtils> TL = ThreadLocal
			.withInitial(() -> new AStarUtils());

	private class AStarUtils {

		IntOpenHashSet closedSet = new IntOpenHashSet();
		IntOpenHashSet openSet = new IntOpenHashSet();
		Int2FloatOpenHashMap gScore = new Int2FloatOpenHashMap();
		NodeDistPriorityQueue fScore = new ClassicNodeDistancePQ();
		Int2IntOpenHashMap cameFrom = new Int2IntOpenHashMap();
		// Priority queue with objects:{ id, distance }

		public AStarUtils()
		{
			gScore.defaultReturnValue(Float.POSITIVE_INFINITY);
			cameFrom.defaultReturnValue(Integer.MIN_VALUE);
		}

		public void clear()
		{
			closedSet.clear();
			openSet.clear();
			gScore.clear();
			fScore.clear();
			cameFrom.clear();
		}

	}

	private static interface NodeDistPriorityQueue {

		void clear();

		int poll();

		void add(int start, float distance);

	}

	private class ClassicNodeDistancePQ implements NodeDistPriorityQueue {

		private static final float INFINITY = Float.POSITIVE_INFINITY;
		private AbstractIntPriorityQueue queue;
		private float[] distances;

		public ClassicNodeDistancePQ()
		{
			this.distances = new float[adjList.length];
			this.queue = new IntHeapPriorityQueue(new IntComparator() {
				@Override
				public int compare(Integer o1, Integer o2)
				{
					return Float.compare(distances[o1], distances[o2]);
				}

				@Override
				public int compare(int k1, int k2)
				{
					return Float.compare(distances[k1], distances[k2]);
				}
			});

			Arrays.fill(distances, INFINITY);
		}

		@Override
		public void clear()
		{
			this.queue.clear();
			Arrays.fill(distances, INFINITY);
		}

		@SuppressWarnings("unused")
		public int peekID()
		{
			return this.queue.firstInt();
		}

		@SuppressWarnings("unused")
		public float peekDistance()
		{
			return this.distances[this.queue.firstInt()];
		}

		@Override
		public void add(int id, float distance)
		{

			if (!this.queue.isEmpty() && this.peekID() == id)
			{
				this.distances[id] = distance;
				this.queue.changed();
			} else if (this.distances[id] == INFINITY)
			{
				this.distances[id] = distance;
				this.queue.enqueue(id);
			}
		}

		@Override
		public int poll()
		{
			final int id = this.queue.dequeueInt();
			while (this.queue.size() > 0 && this.queue.firstInt() == id)
			{
				this.queue.dequeueInt();
			}
			// this.distances.remove(id);
			this.distances[id] = INFINITY;
			return id;
		}

	}

}
