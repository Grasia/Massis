package com.massisframework.massis3.commons.pathfinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.jme3.ai.navmesh.ICell;
import com.jme3.bounding.BoundingBox;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.util.TempVars;
import com.massisframework.massis3.commons.collections.CollectionsFactory;
import com.massisframework.massis3.commons.collections.map.ConstrainedPointMap;
import com.massisframework.massis3.commons.pathfinding.navmesh.NavigationMesh;
import com.massisframework.massis3.commons.pathfinding.navmesh.impl.DefaultNavMeshFactory;
import com.massisframework.massis3.commons.spatials.LinesUtils;

public class GridOverNavMesh implements NavigationMesh<CircleCell> {

	private final NavigationMesh<ICell> baseNavMesh;
	private final List<CircleCell> cells;
	private final Map<Integer, List<Integer>> circleCellsInICells;
	private final ConstrainedPointMap<Integer> cellReferenceMap;
	private final float radius;
	private final float epsilon;

	public GridOverNavMesh(final Mesh rawMesh, final float radius)
	{
		this(new DefaultNavMeshFactory().buildNavigationMesh(rawMesh), radius);
	}

	public GridOverNavMesh(final NavigationMesh<ICell> baseNavMesh,
			final float radius)
	{
		this.radius = radius;
		this.baseNavMesh = baseNavMesh;
		this.circleCellsInICells = CollectionsFactory
				.createMapOfLists(Integer.class, Integer.class);
		// HARDCODED
		this.epsilon = 0.05f;

		this.cells = new ArrayList<>();
		final BoundingBox bb = (BoundingBox) baseNavMesh.getUnderlayingMesh()
				.getBound();
		this.cellReferenceMap = new ConstrainedPointMap<>(Integer.class,
				bb.getMin(new Vector3f()),
				bb.getMax(new Vector3f()),
				radius + epsilon);
		this.createCells();

	}

	@Override
	public void clear()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int getNumCells()
	{
		return this.cells.size();
	}

	@Override
	public CircleCell getCell(final int index)
	{
		return this.cells.get(index);
	}

	// TODO BRUTE FORCE!!!
	@Override
	public CircleCell findClosestCell(final Vector3f point)
	{
		CircleCell nearest = cells.get(0);
		float minDist = Float.MAX_VALUE;
		for (final CircleCell c : this.cells)
		{
			final float dist = c.distance(point);
			if (dist < minDist)
			{
				minDist = dist;
				nearest = c;
			}
		}

		return nearest;

	}

	private void addCell(final ICell reference,  Vector3f center)
	{
		synchronized (this)
		{
			if (!this.cellReferenceMap.containsKey(center))
			{
				center=center.clone();
				final int referenceIndex = reference.getIndex();
				final long cellUUID = this.cellReferenceMap.toGridIndex(center);
				// center = new Vector3f();
				// this.cellReferenceMap.fromGridIndex(cellUUID, center);
				final CircleCell cell = new CircleCell(
						this,
						referenceIndex,
						center,
						cellUUID,
						this.radius,
						cells.size());
				cells.add(cell);
				List<Integer> cellsContained = this.circleCellsInICells
						.get(referenceIndex);
				if (cellsContained == null)
				{
					cellsContained = CollectionsFactory.createList(Integer.class);
					this.circleCellsInICells.put(referenceIndex, cellsContained);
				}
				cellsContained.add(cell.getIndex());
				this.cellReferenceMap.put(center, cell.getIndex());
			}
		}

		// this.cellMap.put(center, cell);
	}

	private void createCells()
	{
		final int nc = this.baseNavMesh.getNumCells();
		IntStream.range(0, nc).parallel().forEach(i -> {
			final ICell cell = this.baseNavMesh.getCell(i);
			fillTri(cell, radius);
			addCell(cell, cell.getCenter());
			for (int j = 0; j < cell.getNumLinks(); j++)
			{
				if (cell.getLink(j) != null)
				{
					int splits = 10;
					for (int pIndex = 0; pIndex < splits; pIndex++)
					{
						addCell(cell,
								LinesUtils.midPointPercentage(cell.getVertex(j),
										cell.getVertex(
												(j + 1) % cell.getNumLinks()),
										(1f / splits) * pIndex));
					}
				}
			}

		});

	}

	protected ICell getICell(final int icellIndex)
	{
		return this.baseNavMesh.getCell(icellIndex);
	}

	protected List<Integer> getCircleCellsInICell(final int icellIndex)
	{
		return this.circleCellsInICells
				.getOrDefault(icellIndex,
						Collections.emptyList());
	}

	// http://stackoverflow.com/a/2049593/3315914
	private void fillTri(final ICell reference, final float radius)
	{
		final TempVars tmp = TempVars.get();
		final Vector3f[] triangle = reference.getTriangle();
		// 1. midpoints tri0 & tri1
		final Vector3f min = tmp.vect1;
		final Vector3f max = tmp.vect2;
		findMinMax(triangle, min, max);

		for (float x = min.x; x < max.x; x += radius)
		{
			for (float z = min.z; z < max.z; z += radius)
			{
				final Vector3f pt = tmp.vect3.set(x, triangle[0].y, z);
				if (PointInTriangle(pt, triangle))
				{
					addCell(reference, pt);
				}
			}
		}
		tmp.release();
	}

	private void findMinMax(final Collection<Vector3f> l, final Vector3f min,
			final Vector3f max)
	{
		findMinMax(l.stream(), min, max);
	}

	private void findMinMax(final Vector3f[] l, final Vector3f min,
			final Vector3f max)
	{
		findMinMax(Arrays.stream(l), min, max);
	}

	private void findMinMax(final Stream<Vector3f> s, final Vector3f min,
			final Vector3f max)
	{
		min.set(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
		max.set(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
		s.sequential().forEach(v -> {
			min.x = Math.min(min.x, v.x);
			min.y = Math.min(min.y, v.y);
			min.z = Math.min(min.z, v.z);

			max.x = Math.max(max.x, v.x);
			max.y = Math.max(max.y, v.y);
			max.z = Math.max(max.z, v.z);
		});
	}

	float sign(final Vector3f p1, final Vector3f p2, final Vector3f p3)
	{
		return (p1.x - p3.x) * (p2.z - p3.z) - (p2.x - p3.x) * (p1.z - p3.z);
	}

	boolean PointInTriangle(final Vector3f pt, final Vector3f[] tri)
	{
		final Vector3f v1 = tri[0];
		final Vector3f v2 = tri[1];
		final Vector3f v3 = tri[2];
		boolean b1, b2, b3;

		b1 = sign(pt, v1, v2) < 0.0f;
		b2 = sign(pt, v2, v3) < 0.0f;
		b3 = sign(pt, v3, v1) < 0.0f;

		return b1 == b2 && b2 == b3;
	}

	@Override
	public void loadFromMesh(final Mesh mesh)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void write(final JmeExporter e) throws IOException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void read(final JmeImporter e) throws IOException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Mesh getUnderlayingMesh()
	{
		return this.baseNavMesh.getUnderlayingMesh();
	}

	@Override
	public boolean isInLineOfSight(final CircleCell StartCell,
			final Vector3f StartPos,
			final Vector3f EndPos)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeCell(final CircleCell c)
	{
		throw new UnsupportedOperationException();
	}

	public List<CircleCell> getCells()
	{
		return this.cells;
	}

}
