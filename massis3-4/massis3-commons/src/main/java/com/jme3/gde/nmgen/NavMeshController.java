/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.gde.nmgen;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.critterai.nmgen.IntermediateData;

import com.jme3.app.Application;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.terrain.Terrain;

import jme3tools.optimize.GeometryBatchFactory;

/**
 *
 * @author sploreg
 */
public class NavMeshController {
	private final Node rootNode;
	@SuppressWarnings("unused")
	private final Application app;

	public NavMeshController(final Node rootNode)
	{
		this(null, rootNode);
	}

	public NavMeshController(final Application app, final Node rootNode)
	{
		this.app = app;
		this.rootNode = rootNode;
	}

	protected void cleanup()
	{

	}

	public List<List<Vector3f>> rpax_getPolys(final NavMeshGenerator generator)
	{
		@SuppressWarnings("unused")
		final IntermediateData id = new IntermediateData();

		generator.setIntermediateData(null);

		final Mesh mesh = new Mesh();
		// NavMesh navMesh = new NavMesh();

		GeometryBatchFactory.mergeGeometries(
				findGeometries(rootNode, new ArrayList<Geometry>(), generator),
				mesh);
		return generator.rpax_getPolys(mesh);
	}

	public Mesh generateNavMesh(final NavMeshGenerator generator)
	{
		@SuppressWarnings("unused")
		final IntermediateData id = new IntermediateData();

		generator.setIntermediateData(id);

		final Mesh mesh = new Mesh();
		// NavMesh navMesh = new NavMesh();

		GeometryBatchFactory.mergeGeometries(
				findGeometries(rootNode, new ArrayList<Geometry>(), generator),
				mesh);
		final Mesh optiMesh = generator.optimize(mesh);

		// final Geometry navMesh = new Geometry("NavMesh");
		// navMesh.setMesh(optiMesh);
		// navMesh.setCullHint(CullHint.Always);
		// navMesh.setModelBound(new BoundingBox());
		//
		// Spatial previous = rootNode.getChild("NavMesh");
		// if (previous != null)
		// previous.removeFromParent();
		//
		// app.enqueue(new Callable<Void>() {
		// public Void call() throws Exception {
		// rootNode.attachChild(navMesh);
		// return null;
		// }
		// });

		// jmeRootNode.refresh(true);

		return optiMesh;
	}

	public Mesh generateNavMesh(final float cellSize, final float cellHeight,
			final float minTraversableHeight, final float maxTraversableStep,
			final float maxTraversableSlope, final boolean clipLedges,
			final float traversableAreaBorderSize,
			final float smoothingThreshold,
			final boolean useConservativeExpansion,
			final float minUnconnectedRegionSize,
			final float mergeRegionSize,
			final float maxEdgeLength, final float edgeMaxDeviation,
			final float maxVertsPerPoly,
			final float contourSampleDistance,
			final float contourMaxDeviation)
	{
		final NavMeshGenerator generator = new NavMeshGenerator();
		generator.setCellSize(cellSize);
		generator.setCellHeight(cellHeight);
		generator.setMinTraversableHeight(minTraversableHeight);
		generator.setMaxTraversableStep(maxTraversableStep);
		generator.setMaxTraversableSlope(maxTraversableSlope);
		generator.setClipLedges(clipLedges);
		generator.setTraversableAreaBorderSize(traversableAreaBorderSize);
		generator.setSmoothingThreshold((int) smoothingThreshold);
		generator.setUseConservativeExpansion(useConservativeExpansion);
		generator.setMergeRegionSize((int) mergeRegionSize);
		generator.setMaxEdgeLength(maxEdgeLength);
		generator.setEdgeMaxDeviation(edgeMaxDeviation);
		generator.setMaxVertsPerPoly((int) maxVertsPerPoly);
		generator.setContourSampleDistance(contourSampleDistance);
		generator.setContourMaxDeviation(contourMaxDeviation);
		return generateNavMesh(generator);
	}

	private List<Geometry> findGeometries(final Node node,
			final List<Geometry> geoms,
			final NavMeshGenerator generator)
	{
		for (final Iterator<Spatial> it = node.getChildren().iterator(); it
				.hasNext();)
		{
			final Spatial spatial = it.next();
			if (spatial instanceof Geometry)
			{
				geoms.add((Geometry) spatial);
			} else if (spatial instanceof Node)
			{
				if (spatial instanceof Terrain)
				{
					final Mesh merged = generator
							.terrain2mesh((Terrain) spatial);
					final Geometry g = new Geometry("mergedTerrain");
					g.setMesh(merged);
					geoms.add(g);
				} else
					findGeometries((Node) spatial, geoms, generator);
			}
		}
		return geoms;
	}

	// private Material getNavMaterial() {
	// if (navMaterial != null)
	// return navMaterial;
	// navMaterial = new Material(app.getAssetManager(),
	// "Common/MatDefs/Misc/Unshaded.j3md");
	// navMaterial.setColor("Color", ColorRGBA.Green);
	// navMaterial.getAdditionalRenderState().setWireframe(true);
	// return navMaterial;
	// }

}
