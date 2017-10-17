package com.jme3.ai.navmesh;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import com.jme3.ai.navmesh.Cell.ClassifyResult;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.mesh.IndexBuffer;
import com.jme3.util.BufferUtils;
import com.massisframework.massis3.commons.pathfinding.navmesh.NavigationMesh;

/**
 * A NavigationMesh is a collection of NavigationCells used to control object
 * movement while also providing path finding line-of-sight testing. It serves
 * as a parent to all the Actor objects which exist upon it.
 * 
 * Portions Copyright (C) Greg Snook, 2000
 * 
 * @author TR
 * 
 */
public class NavMesh implements Savable, NavigationMesh<ICell> {

	/**
	 * the cells that make up this mesh
	 */
	private ArrayList<Cell> cellList = new ArrayList<Cell>();
	private Mesh underlayingMesh;

	public NavMesh()
	{
	}

	public NavMesh(final Mesh mesh)
	{
		loadFromMesh(mesh);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jme3.ai.navmesh.NavigationMesh#clear()
	 */
	@Override
	public void clear()
	{
		cellList.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jme3.ai.navmesh.NavigationMesh#addCell(com.jme3.math.Vector3f,
	 * com.jme3.math.Vector3f, com.jme3.math.Vector3f)
	 */
	public void addCell(final Vector3f pointA, final Vector3f PointB,
			final Vector3f PointC,
			final int index)
	{
		final Cell newCell = new Cell();
		newCell.initialize(pointA.clone(), PointB.clone(), PointC.clone(),
				index);
		cellList.add(newCell);
	}

	/**
	 * Does noting at this point. Stubbed for future use in animating the mesh
	 * 
	 * @param elapsedTime
	 */
	void Update(final float elapsedTime)
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jme3.ai.navmesh.NavigationMesh#getNumCells()
	 */
	@Override
	public int getNumCells()
	{
		return cellList.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jme3.ai.navmesh.NavigationMesh#getCell(int)
	 */
	@Override
	public ICell getCell(final int index)
	{
		return cellList.get(index);
	}

	/**
	 * Force a point to be inside the nearest cell on the mesh
	 */
	Vector3f snapPointToMesh(final Vector3f point)
	{
		return findClosestCell(point).snapPoint(point);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jme3.ai.navmesh.NavigationMesh#findClosestCell(com.jme3.math.
	 * Vector3f)
	 */
	@Override
	public Cell findClosestCell(final Vector3f point)
	{
		float closestDistance = 3.4E+38f;
		float closestHeight = 3.4E+38f;
		boolean foundHomeCell = false;
		float thisDistance;
		Cell closestCell = null;

		// oh dear this is not fast
		for (final Cell cell : cellList)
		{
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
				final Vector2f start = new Vector2f(cell.getCenter().x,
						cell.getCenter().z);
				final Vector2f end = new Vector2f(point.x, point.z);
				final Line2D motionPath = new Line2D(start, end);

				final ClassifyResult Result = cell
						.classifyPathToCell(motionPath);

				if (Result.result == ICell.PathResult.ExitingCell)
				{
					Vector3f ClosestPoint3D = new Vector3f(
							Result.intersection.x, 0.0f, Result.intersection.y);
					cell.computeHeightOnCell(ClosestPoint3D);

					ClosestPoint3D = ClosestPoint3D.subtract(point);

					thisDistance = ClosestPoint3D.length();

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

	/**
	 * Test to see if two points on the mesh can view each other FIXME: EndCell
	 * is the last visible cell?
	 *
	 * @param StartCell
	 * @param StartPos
	 * @param EndPos
	 * @return
	 */
	@Override
	public boolean isInLineOfSight(final ICell StartCell,
			final Vector3f StartPos,
			final Vector3f EndPos)
	{
		return isInLineOfSight(StartCell, StartPos, EndPos, null);
	}

	boolean isInLineOfSight(final ICell StartCell, final Vector3f StartPos,
			final Vector3f EndPos,
			final DebugInfo debugInfo)
	{
		final Line2D MotionPath = new Line2D(
				new Vector2f(StartPos.x, StartPos.z),
				new Vector2f(EndPos.x, EndPos.z));

		final ICell testCell = StartCell;
		Cell.ClassifyResult result = testCell.classifyPathToCell(MotionPath);
		Cell.ClassifyResult prevResult = result;

		while (result.result == ICell.PathResult.ExitingCell)
		{
			if (result.cell == null)// hit a wall, so the point is not visible
			{
				if (debugInfo != null)
				{
					debugInfo.setFailedCell(prevResult.cell);
				}
				return false;
			}
			if (debugInfo != null)
			{
				debugInfo.addPassedCell(prevResult.cell);
			}
			prevResult = result;
			result = result.cell.classifyPathToCell(MotionPath);
		}
		if (debugInfo != null)
		{
			debugInfo.setEndingCell(prevResult.cell);
		}
		return result.result == ICell.PathResult.EndingCell
				|| result.result == ICell.PathResult.ExitingCell; // This is
																	// messing
																	// up the
																	// result, I
																	// think
																	// because
																	// of shared
																	// borders
	}

	public void linkCells()
	{

		for (final Cell pCellA : cellList)
		{
			for (final Cell pCellB : cellList)
			{
				if (pCellA != pCellB)
				{
					pCellA.checkAndLink(pCellB, 0.001f);
				}
			}
		}
	}

	private void addFace(final Vector3f vertA, final Vector3f vertB,
			final Vector3f vertC,
			final int index)
	{
		// some art programs can create linear polygons which have two or more
		// identical vertices. This creates a poly with no surface area,
		// which will wreak havok on our navigation mesh algorithms.
		// We only except polygons with unique vertices.
		if (!vertA.equals(vertB) && !vertB.equals(vertC)
				&& !vertC.equals(vertA))
		{
			addCell(vertA, vertB, vertC, index);
		} else
		{
			System.out.println("Warning, Face winding incorrect");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jme3.ai.navmesh.NavigationMesh#loadFromMesh(com.jme3.scene.Mesh)
	 */
	@Override
	public void loadFromMesh(final Mesh mesh)
	{
		clear();

		final Vector3f a = new Vector3f();
		final Vector3f b = new Vector3f();
		final Vector3f c = new Vector3f();

		final Plane up = new Plane();
		up.setPlanePoints(Vector3f.UNIT_X, Vector3f.ZERO, Vector3f.UNIT_Z);
		up.getNormal();

		final IndexBuffer ib = mesh.getIndexBuffer();
		final FloatBuffer pb = mesh.getFloatBuffer(Type.Position);
		pb.clear();
		for (int i = 0; i < mesh.getTriangleCount() * 3; i += 3)
		{
			final int i1 = ib.get(i + 0);
			final int i2 = ib.get(i + 1);
			final int i3 = ib.get(i + 2);
			BufferUtils.populateFromBuffer(a, pb, i1);
			BufferUtils.populateFromBuffer(b, pb, i2);
			BufferUtils.populateFromBuffer(c, pb, i3);

			final Plane p = new Plane();
			p.setPlanePoints(a, b, c);
			if (up.pseudoDistance(p.getNormal()) <= 0.0f)
			{
				System.out.println(
						"Warning, normal of the plane faces downward!!!");
				continue;
			}

			addFace(a, b, c, i / 3);
		}

		linkCells();
		this.underlayingMesh = mesh;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.jme3.ai.navmesh.NavigationMesh#write(com.jme3.export.JmeExporter)
	 */
	@Override
	public void write(final JmeExporter e) throws IOException
	{
		final OutputCapsule capsule = e.getCapsule(this);
		capsule.writeSavableArrayList(cellList, "cellarray", null);
		capsule.write(this.underlayingMesh, "underlayingMesh", null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jme3.ai.navmesh.NavigationMesh#read(com.jme3.export.JmeImporter)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void read(final JmeImporter e) throws IOException
	{
		final InputCapsule capsule = e.getCapsule(this);
		cellList = capsule.readSavableArrayList("cellarray",
				new ArrayList<Cell>());
		underlayingMesh = (Mesh) capsule.readSavable("underlayingMesh", null);
	}

	@Override
	public Mesh getUnderlayingMesh()
	{
		this.underlayingMesh.updateCounts();
		this.underlayingMesh.updateBound();
		
		return underlayingMesh;
	}

	@Override
	public void removeCell(final ICell c)
	{
		for (int j = 0; j < 3; j++)
		{
			if (c.getLink(j) != null)
			{
				for (int j2 = 0; j2 < 3; j2++)
				{
					if (c.getLink(j).getLink(j2) == c)
					{
						c.getLink(j).setLink(j2, null);
					}
				}
			}
		}
		this.cellList.remove(c);
	}

}
