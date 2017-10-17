package com.massisframework.massis3.commons.pathfinding;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.util.TempVars;
import com.massisframework.massis3.commons.pathfinding.ArrayQuadTree.CoordinateRetriever;
import com.massisframework.massis3.commons.pathfinding.navmesh.FindPathResult;

import it.unimi.dsi.fastutil.ints.AbstractIntPriorityQueue;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntHeapPriorityQueue;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public class UniformGridPathFinder {

	private static final Logger log = LoggerFactory.getLogger(UniformGridPathFinder.class);
	private UniformGridGraph ug;
	private ArrayQuadTree<Integer> quadP;
	private Int2FloatMap weights;

	public UniformGridPathFinder(UniformGridGraph ug)
	{
		this.ug = ug;
		this.weights = new Int2FloatOpenHashMap();
		this.weights.defaultReturnValue(0f);
		// buildQuadTree();

	}

	// TODO disminucion con distancia
	public void setWeight(int nodeId, float weight)
	{
		this.weights.put(nodeId, weight);
	}

	public float getWeight(int nodeId)
	{
		return this.weights.get(nodeId);
	}

	private void buildQuadTree()
	{

		float minX = Float.MAX_VALUE;
		float maxX = Float.MAX_VALUE;
		float minY = Float.MIN_VALUE;
		float maxY = Float.MIN_VALUE;

		CoordinateRetriever<Integer> coordRetriever = new ArrayQuadTree.CoordinateRetriever<Integer>() {

			@Override
			public float getX(Integer e)
			{
				return ug.getNodePositionX(e);
			}

			@Override
			public float getZ(Integer e)
			{
				return ug.getNodePositionZ(e);
			}
		};

		for (int i = 0; i < ug.getNumNodes(); i++)
		{
			minX = Math.min(ug.getNodePositionX(i), minX);
			maxX = Math.max(ug.getNodePositionX(i), maxX);

			minY = Math.min(ug.getNodePositionZ(i), minY);
			maxY = Math.max(ug.getNodePositionZ(i), maxY);
		}

		this.quadP = new ArrayQuadTree<>(8, (int) minX, (int) maxX, (int) minY, (int) maxY,
				coordRetriever);
		for (int i = 0; i < ug.getNumNodes(); i++)
		{

			if (log.isInfoEnabled())
			{
				log.info("Inserting node {}", i);
			}

			this.quadP.insert(i);
		}

	}

	public FindPathResult findPath(Vector3f start, Vector3f goal, final IntList store)
	{
		return this.findPath(findNearestCell(start), findNearestCell(goal), store);
	}

	public int findNearestCell(Vector3f p)
	{
		// TODO FIXME very inneficient
		float minDist = Float.MAX_VALUE;
		int nearest = -1;
		TempVars tmp = TempVars.get();
		for (int i = 0; i < ug.getNumNodes(); i++)
		{
			Vector3f nodePos = ug.getNodePosition(i, tmp.vect1);
			float dist = p.distanceSquared(nodePos);
			if (dist < minDist)
			{
				minDist = dist;
				nearest = i;
			}
		}
		tmp.release();
		return nearest;
	}

	public FindPathResult findPath(int start, int goal, final IntList store)
	{
		final AStarUtils utils = TL.get();
		utils.clear();
		/*
		 * Initialization
		 */
		// The set of nodes already evaluated.
		final IntOpenHashSet closedSet = utils.closedSet;
		final IntOpenHashSet openSet = utils.openSet;

		// The set of currently discovered nodes still to be evaluated.
		// Initially, only the start node is known.
		openSet.add(start);
		// For each node, which node it can most efficiently be reached from.
		// If a node can be reached from many nodes, cameFrom will eventually
		// contain the
		// most efficient previous step.
		final Int2IntOpenHashMap cameFrom = utils.cameFrom;
		// For each node, the cost of getting from the start node to that node.
		final Int2FloatOpenHashMap gScore = utils.gScore;
		// The cost of going from start to start is zero.
		gScore.put(start, 0);
		// For each node, the total cost of getting from the start node to the
		// goal
		// by passing by that node. That value is partly known, partly
		// heuristic.
		final NodeDistPriorityQueue fScore = utils.fScore;
		// For the first node, that value is completely heuristic.
		fScore.add(start, heuristic_cost_estimate(start, goal));
		int expandedNodes = 0;
		while (!openSet.isEmpty())
		{
			expandedNodes = Math.max(fScore.size(), expandedNodes);
			// final int current = 0;// := the node in openSet having the lowest
			// fScore[] value
			// AdjListCache adjListCache = adjListCacheTL.get();
			final int current = fScore.poll();
			if (closedSet.contains(current))
				continue;
			closedSet.add(current);
			openSet.remove(current);

			if (current == goal)
			{
//				if (log.isInfoEnabled())
//				{
//					log.info("Expanded nodes: {}", expandedNodes);
//				}
				reconstruct_path(cameFrom, current, store);
				return FindPathResult.COMPLETE_PATH_FOUND;
			}

			for (int n = 0; n < this.ug.getNumLinks(current); n++)
			{
				final int neighbor = this.ug.getLinkId(current, n);

				if (closedSet.contains(neighbor))
				{
					continue;// Ignore the neighbor which is already evaluated.
				}
				// The distance from start to a neighbor
				final float tentative_gScore = gScore.get(current)
						+ euclideanDistance(current, neighbor);
				if (tentative_gScore > gScore.get(neighbor)
						|| FastMath.approximateEquals(tentative_gScore, gScore.get(neighbor)))
				{
					continue;
				} else
				{
					openSet.add(neighbor);
					cameFrom.put(neighbor, current);
					gScore.put(neighbor, tentative_gScore);
					fScore.add(neighbor,
							gScore.get(neighbor) + heuristic_cost_estimate(neighbor, goal));
				}

			}
		}
		utils.clear();

		return FindPathResult.NOT_FOUND;
	}

	private float euclideanDistance(int nodeA, int nodeB)
	{
		TempVars tmp = TempVars.get();
		Vector3f a = this.ug.getNodePosition(nodeA, tmp.vect1);
		boolean border = this.ug.getNumLinks(nodeA) < 8 || this.ug.getNumLinks(nodeB) < 8;
		Vector3f b = this.ug.getNodePosition(nodeB, tmp.vect2);
		float dist = a.distance(b);
		tmp.release();
		// if (isBorder(nodeB))
		// dist *= 16;
		// else if (isSemiBorder(nodeB))
		// dist *= 8;
		return dist;
	}

	private boolean isBorder(int node)
	{
		return this.ug.getNumLinks(node) < 8;
	}

	private boolean isSemiBorder(int node)
	{
		if (isBorder(node))
			return true;
		AtomicBoolean b = new AtomicBoolean(false);
		this.ug.forEachLink(node, (nId, pos) -> {
			if (isBorder(nId))
			{
				b.set(true);
			}
		});
		return b.get();
	}

	private float manhattanDist(int nodeA, int nodeB)
	{
		TempVars tmp = TempVars.get();
		Vector3f a = this.ug.getNodePosition(nodeA, tmp.vect1);
		Vector3f b = this.ug.getNodePosition(nodeB, tmp.vect2);
		final float dist = Math.abs(a.x - a.x) + Math.abs(a.z - b.z);
		tmp.release();
		return dist;
	}

	private IntList reconstruct_path(final Int2IntOpenHashMap cameFrom,
			int current,
			final IntList store)
	{
		// store.clear(); -> No because we accumulate partial results here.
		final IntList total_path = store;// new IntArrayList();
		total_path.add(current);
		while (cameFrom.containsKey(current))
		{
			current = cameFrom.get(current);
			total_path.add(current);
		}
		return total_path;
	}

	private final float heuristic_cost_estimate(final int start, final int goal)
	{
		return euclideanDistance(start, goal) / 16;
	}

	private final ThreadLocal<AStarUtils> TL = ThreadLocal
			.withInitial(() -> new AStarUtils());

	private class AStarUtils {

		IntOpenHashSet closedSet = new IntOpenHashSet();
		IntOpenHashSet openSet = new IntOpenHashSet();
		Int2FloatOpenHashMap gScore = new Int2FloatOpenHashMap();
		NodeDistPriorityQueue fScore = new SimpleDistancePQ();
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

		int size();

		int poll();

		void add(int start, float distance);

	}

	

	private class SimpleDistancePQ implements NodeDistPriorityQueue {

		private final PriorityQueue<Integer> queue;
		private final Int2FloatOpenHashMap distMap;

		public SimpleDistancePQ()
		{

			this.distMap = new Int2FloatOpenHashMap();
			this.distMap.defaultReturnValue(Float.POSITIVE_INFINITY);
			this.queue = new PriorityQueue<>(4096, this::compareInts);

		}

		@Override
		public void clear()
		{
			this.queue.clear();
			this.distMap.clear();
		}

		private int compareInts(int k1, int k2)
		{
			return Float.compare(distMap.get(k1), distMap.get(k2));
		}

		@Override
		public int poll()
		{
			return this.queue.poll();
		}

		@Override
		public void add(int start, float distance)
		{

			if (!this.distMap.containsKey(start))
			{
				this.distMap.put(start, distance);
				this.queue.add(start);
			} else
			{
				this.queue.removeIf(item -> item == start);
				this.distMap.put(start, distance);
				this.queue.add(start);
			}
		}

		private String queueToString()
		{
			try
			{
				StringBuilder sb = new StringBuilder();
				Field f = IntHeapPriorityQueue.class.getDeclaredField("heap");
				f.setAccessible(true);
				int[] array = (int[]) f.get(queue);
				for (int i = 0; i < queue.size(); i++)
				{
					sb.append(array[i] + ",");
				}
				return "[" + sb.toString() + "]";
			} catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}

		@Override
		public int size()
		{
			return this.queue.size();
		}

	}

	private class ClassicNodeDistancePQ implements NodeDistPriorityQueue {

		private static final float INFINITY = Float.POSITIVE_INFINITY;
		private AbstractIntPriorityQueue queue;
		private float[] distances;

		public ClassicNodeDistancePQ()
		{
			this.distances = new float[ug.getNumNodes()];
			this.queue = new IntHeapPriorityQueue(new IntComparator() {
				@Override
				public int compare(final Integer o1, final Integer o2)
				{
					return Float.compare(distances[o1], distances[o2]);
				}

				@Override
				public int compare(final int k1, final int k2)
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
		public void add(final int id, final float distance)
		{

			if (!this.queue.isEmpty() && this.peekID() == id)
			{
				this.distances[id] = distance;
				this.queue.changed();
			} else if (!Float.isFinite(this.distances[id]))
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

		@Override
		public int size()
		{
			return this.queue.size();
		}

	}
}
