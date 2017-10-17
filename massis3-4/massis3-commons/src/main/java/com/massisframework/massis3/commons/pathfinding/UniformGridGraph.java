package com.massisframework.massis3.commons.pathfinding;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.ai.navmesh.ICell;
import com.jme3.ai.navmesh.NavMesh;
import com.jme3.bounding.BoundingBox;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;
import com.jme3.util.TempVars;
import com.massisframework.massis3.commons.collections.map.ConstrainedPointMap;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public class UniformGridGraph {

	private static final Logger log = LoggerFactory.getLogger(UniformGridGraph.class);

	private float cellSize;
	private List<Vector3f> nodePoints;
	private int[][] neighTable;
	private transient int numEdges = -1;


	public UniformGridGraph()
	{
	}

	public int getNumEdges()
	{
		if (numEdges < 0)
		{
			int count = 0;
			for (int i = 0; i < neighTable.length; i++)
			{
				for (int j = 0; j < neighTable[i].length; j++)
				{
					count++;
				}
			}
			numEdges = count;
		}
		return numEdges;
	}

	public Vector3f getNodePosition(int nodeId, Vector3f store)
	{
		return store.set(this.nodePoints.get(nodeId));
	}

	public float getNodePositionX(int nodeId)
	{
		return this.nodePoints.get(nodeId).x;
	}

	public float getNodePositionY(int nodeId)
	{
		return this.nodePoints.get(nodeId).y;
	}

	public float getNodePositionZ(int nodeId)
	{
		return this.nodePoints.get(nodeId).z;
	}

	public int getNumLinks(int nodeId)
	{
		return this.neighTable[nodeId].length;
	}

	public int getLinkId(int nodeId, int linknum)
	{
		return this.neighTable[nodeId][linknum];
	}

	public int getNumNodes()
	{
		return this.nodePoints.size();
	}

	public Vector3f getRandomNodePosition()
	{
		return this.getNodePosition(FastMath.nextRandomInt(0, this.getNumNodes() - 1),
				new Vector3f());
	}

	public Iterable<Integer> getLinkIds(int nodeId)
	{
		return Arrays.stream(this.neighTable[nodeId])::iterator;
	}

	public Vector3f getNeighborPosition(int nodeId, int linknum, Vector3f store)
	{
		return getNodePosition(getLinkId(nodeId, linknum), store);
	}

	public void forEachLink(int nodeId, BiConsumer<Integer, Vector3f> action)
	{
		int nLinks = getNumLinks(nodeId);
		for (int i = 0; i < nLinks; i++)
		{
			final int linkId = getLinkId(nodeId, i);

			final Vector3f neighPos = this.nodePoints.get(linkId);

			final float x = neighPos.x;
			final float y = neighPos.y;
			final float z = neighPos.z;

			action.accept(linkId, neighPos);

			neighPos.x = x;
			neighPos.y = y;
			neighPos.z = z;
		}
	}

	public void build(NavMesh nm, float cellSize)
	{
		this.cellSize = cellSize;
		this.nodePoints = new ArrayList<>();
		BoundingBox bounds = getGridMinMax(nm);
		ConstrainedPointMap<Integer> nodePositionMap = new ConstrainedPointMap<>(Integer.class,
				bounds.getMin(new Vector3f()),
				bounds.getMax(new Vector3f()),
				this.cellSize);
		scanAndBuild(nm, nodePositionMap);
		createLinks(nodePositionMap);
	}

	private void createLinks(ConstrainedPointMap<Integer> nodePositionMap)
	{
		this.neighTable = new int[this.nodePoints.size()][];
		final int offsetMin = -1;
		final int offsetMax = 1;
		final IntArrayList neighbors_tmp = new IntArrayList();
		for (int i = 0; i < this.nodePoints.size(); i++)
		{
			neighbors_tmp.clear();
			/** @formatter:off**/
			for (int x_off = offsetMin; x_off <= offsetMax; x_off++)
			for (int y_off = offsetMin; y_off <= offsetMax; y_off++)
			for (int z_off = offsetMin; z_off <= offsetMax; z_off++)
			{
				if (x_off == 0 && y_off == 0 && z_off == 0)
					continue;
//				if (x_off!=0 && z_off!=0) continue;
					
					final Vector3f nodePoint = this.nodePoints.get(i);
					final Integer nId = nodePositionMap.get(
							nodePoint.x + x_off*this.cellSize,
							nodePoint.y + y_off*this.cellSize,
							nodePoint.z + z_off*this.cellSize);
					if (nId != null && nId!=i)
					{
						if (!neighbors_tmp.contains(nId))
						neighbors_tmp.add(nId);
					}
				}
			neighTable[i]=neighbors_tmp.toIntArray();
			/** @formatter:on**/
		}

	}

	private void scanAndBuild(NavMesh nm, ConstrainedPointMap<Integer> nodePositionMap)
	{
		Map<Integer, float[][]> elevsMinMax = getElevations(nm);
		elevsMinMax.entrySet().parallelStream().forEach(entry -> {
			final float elev = fromIntScaled(entry.getKey());
			final float minX = entry.getValue()[0][0];
			final float minZ = entry.getValue()[0][1];

			final float maxX = entry.getValue()[1][0];
			final float maxZ = entry.getValue()[1][1];

			if (log.isInfoEnabled())
			{
				log.info("scanning with elev " + elev);
			}
			for (float x = minX; x < maxX; x += cellSize)
			{
				for (float z = minZ; z < maxZ; z += cellSize)
				{
					final Vector3f p = new Vector3f(x, elev, z);

					final int icellIndex = snapToCell(nm, p);
					if (icellIndex >= 0)
					{
						synchronized (this)
						{
							if (!nodePositionMap.containsKey(p))
							{
								final int nodeId = this.nodePoints.size();
								nodePositionMap.put(p, nodeId);
								this.nodePoints.add(p);
							}
						}

					}
				}
			}
		});
		if (log.isInfoEnabled())
		{
			log.info("scanning finished.");
		}

	}

	private int snapToCell(NavMesh nm, final Vector3f point)
	{

		final float old_y = point.y;
		point.y += 0.001f;
		final ICell cell = nm.findClosestCell(point);
		if (cell != null)
		{
			cell.snapPoint(point);
			return cell.getIndex();
		} else
		{
			return -1;
		}
	}

	private Map<Integer, float[][]> getElevations(NavMesh nm)
	{
		Map<Integer, float[][]> elevsMinMax = new Int2ObjectOpenHashMap<>();
		for (int i = 0; i < nm.getNumCells(); i++)
		{
			final ICell cell = nm.getCell(i);
			final Vector3f[] tri = cell.getTriangle();
			for (int j = 0; j < tri.length; j++)
			{
				final int key = toIntScaled(tri[j].y);
				float[][] minMax = elevsMinMax.get(key);
				if (minMax == null)
				{
					minMax = new float[][] {
							{ tri[j].x, tri[j].z },
							{ tri[j].x, tri[j].z }
					};
					elevsMinMax.put(key, minMax);
				}

				minMax[0][0] = Math.min(minMax[0][0], (tri[j].x));
				minMax[0][1] = Math.min(minMax[0][1], (tri[j].z));
				minMax[1][0] = Math.max(minMax[1][0], (tri[j].x));
				minMax[1][1] = Math.max(minMax[1][1], (tri[j].z));
			}
		}
		return elevsMinMax;
	}

	// TODO generateElevations
	private static BoundingBox getGridMinMax(NavMesh nm)
	{
		Vector3f gridMax = new Vector3f(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
		Vector3f gridMin = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
		for (int i = 0; i < nm.getNumCells(); i++)
		{
			final ICell cell = nm.getCell(i);
			final Vector3f[] tri = cell.getTriangle();
			for (int j = 0; j < tri.length; j++)
			{
				final int key = toIntScaled(tri[j].y);
				gridMin.x = Math.min(gridMin.x, tri[j].x);
				gridMin.y = Math.min(gridMin.y, tri[j].y);
				gridMin.z = Math.min(gridMin.z, tri[j].z);

				gridMax.x = Math.max(gridMax.x, tri[j].x);
				gridMax.y = Math.max(gridMax.y, tri[j].y);
				gridMax.z = Math.max(gridMax.z, tri[j].z);
			}
		}
		return new BoundingBox(gridMin, gridMax);
	}

	public Mesh loadUniformGridMesh()
	{
		final Mesh m = new Mesh();
		m.setMode(Mesh.Mode.Lines);
		int nSegments = this.getNumEdges() / 2;
		int nNodes = this.getNumNodes();
		Set<IntIntPair> visited = new HashSet<>(nSegments);
		final FloatBuffer pBuff = BufferUtils
				.createFloatBuffer((this.getNumEdges() * 3) / 2);
		TempVars tmp = TempVars.get();

		for (int i = 0; i < nNodes; i++)
		{
			Vector3f pos = this.getNodePosition(i, tmp.vect1);
			pBuff.put(pos.x);
			pBuff.put(pos.y);
			pBuff.put(pos.z);

			int nLinks = this.getNumLinks(i);
			for (int j = 0; j < nLinks; j++)
			{
				int neighId = this.getLinkId(i, j);
				visited.add(new IntIntPair(i, neighId));
			}
		}
		final IntBuffer indexBuff = BufferUtils.createIntBuffer(visited.size() * 2);
		visited.stream().forEach(pair -> {
			indexBuff.put(pair.a);
			indexBuff.put(pair.b);
		});
		m.setBuffer(Type.Position, 3, pBuff);
		m.setBuffer(Type.Index, 2, indexBuff);
		tmp.release();
		return m;
	}

	private static class IntIntPair {
		int a;
		int b;

		public IntIntPair(int a, int b)
		{
			this.a = a;
			this.b = b;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + a;
			result = prime * result + b;
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			IntIntPair other = (IntIntPair) obj;
			if (a == other.a && b == other.b)
				return true;
			if (a == other.b && b == other.a)
				return true;
			return false;
		}

	}

	public static int toIntScaled(final float f)
	{
		return Math.round(((f) * 10000));
	}

	public static float fromIntScaled(final int l)
	{
		return ((float) l) / 10000;
	}

}
