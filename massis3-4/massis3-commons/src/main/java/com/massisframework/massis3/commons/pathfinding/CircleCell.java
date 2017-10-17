package com.massisframework.massis3.commons.pathfinding;

import java.util.Collection;
import java.util.List;

import com.jme3.ai.navmesh.ICell;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.massisframework.massis3.commons.collections.CollectionsFactory;
import com.massisframework.massis3.commons.pathfinding.navmesh.NavMeshNode;
import com.massisframework.massis3.commons.spatials.LinesUtils;

public class CircleCell implements NavMeshNode<CircleCell> {

	private final Vector3f center;
	private List<CircleCell> neighs;
	private final float radius;
	private final int index;
	private final long constrainedMapUUID;
	private final int referenceCell;
	private final GridOverNavMesh parentMesh;

	public CircleCell(
			final GridOverNavMesh parentMesh,
			final int referenceCell,
			final Vector3f center,
			final long constrainedMapUUID,
			final float radius,
			final int index)
	{
		this.radius = radius;
		this.parentMesh = parentMesh;
		this.referenceCell = referenceCell;
		this.constrainedMapUUID = constrainedMapUUID;
		this.center = center.clone();
		this.index = index;

	}

	protected int getReferenceCellIndex()
	{
		return this.referenceCell;
	}

	@Override
	public CircleCell getLink(final int side)
	{
		return this.getNeighbors().get(side);
	}

	@Override
	public boolean contains(final Vector2f point)
	{
		return LinesUtils.distance2D(this.center, point) <= this.radius;
	}

	@Override
	public boolean contains(final Vector3f point)
	{
		return LinesUtils.distance2D(this.center, point) <= this.radius;
	}

	@Override
	public Vector3f getCenter()
	{
		return this.center;
	}

	@Override
	public int getNumLinks()
	{
		return this.getNeighbors().size();
	}

	public float distance(final CircleCell b)
	{
		return LinesUtils.distance2D(this.center, b.getCenter());
	}

	public float distance(final Vector3f b)
	{
		return LinesUtils.distance2D(this.center, b);
	}

	private void getNeighborsInICell(final ICell icell,
			final List<CircleCell> store,
			final int max)
	{

		final Collection<Integer> circlesInCell = this.parentMesh
				.getCircleCellsInICell(icell.getIndex());
		int added = 0;
		for (final int nIndex : circlesInCell)
		{
			final CircleCell neigh = this.parentMesh.getCell(nIndex);
			final float distAllowed = this.radius + neigh.radius + 0.1f;
			if (this.distance(neigh) <= distAllowed && added < max)
			{
				added++;
				store.add(neigh);
			}
		}
	}

	private void buildNeighbors(final List<CircleCell> store)
	{
		final ICell icell = this.parentMesh
				.getICell(this.getReferenceCellIndex());
		getNeighborsInICell(icell, store, 8);
		for (int i = 0; i < icell.getNumLinks(); i++)
		{
			if (icell.getLink(i) != null)
			{
				getNeighborsInICell(icell.getLink(i), store, 3);
			}
		}
	}

	// private List<CircleCell> sortedNeighbors()
	// {
	// ICell icell = this.parentMesh.getICell(this.getReferenceCellIndex());
	// Stream<CircleCell> s = this.parentMesh
	// .getCircleCellsInICell(icell.getIndex())
	// .stream()
	// .filter(index -> index != this.getIndex())
	// .map(index -> this.parentMesh.getCell(index));
	//
	// for (int i = 0; i < icell.getNumLinks(); i++)
	// {
	// if (icell.getLink(i) != null)
	// {
	// s = Stream.concat(s, this.parentMesh
	// .getCircleCellsInICell(icell.getLink(i).getIndex())
	// .stream()
	// .map(index -> this.parentMesh.getCell(index)));
	// }
	// }
	//
	// s = s.sorted((c1, c2) -> {
	// return Float.compare(c1.distance(this), c2.distance(this));
	// }).limit(8);
	// for (int i = 0; i < icell.getNumLinks(); i++)
	// {
	// if (icell.getLink(i) != null)
	// {
	// int ni = this.parentMesh
	// .getCircleCellsInICell(icell.getLink(i).getIndex())
	// .get(0);
	// s = Stream.concat(s, Stream.of(this.parentMesh.getCell(ni)));
	// }
	// }
	// return s.collect(Collectors.toList());
	//
	// }

	public List<CircleCell> getNeighbors()
	{
		if (this.neighs == null)
		{
			this.neighs = CollectionsFactory.createList(CircleCell.class);
			this.buildNeighbors(neighs);
		}
		return this.neighs;
	}

	public int getIndex()
	{
		return this.index;
	}

	@Override
	public boolean intersectsCircle(final Vector3f point, final float radius)
	{
		return LinesUtils.distance2D(this.center, point) < this.radius + radius;
	}

	public long getConstrainedMapUUID()
	{
		return constrainedMapUUID;
	}

	@Override
	public Vector3f snapPoint(final Vector3f point)
	{
		throw new UnsupportedOperationException();
	}

	public int getReferenceCell()
	{
		return referenceCell;
	}

}
