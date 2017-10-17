package com.massisframework.massis3.commons.pathfinding.navmesh.impl;

import com.jme3.ai.navmesh.ICell;
import com.jme3.ai.navmesh.NavMesh;
import com.jme3.gde.nmgen.NavMeshController;
import com.jme3.gde.nmgen.NavMeshGenerator;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.massisframework.massis3.commons.pathfinding.navmesh.NavigationMesh;
import com.massisframework.massis3.commons.pathfinding.navmesh.NavigationMeshFactory;
import com.massisframework.massis3.commons.pathfinding.navmesh.NavigationMeshPathFinder;

public class DefaultNavMeshFactory implements NavigationMeshFactory<ICell> {

	@Override
	public NavigationMesh<ICell> buildNavigationMesh(final Node world)
	{
		final Mesh m = generateRawMesh(world);
		return buildNavigationMesh(m);
	}
	
	public NavigationMesh<ICell> buildNavigationMesh(final Node world,NavMeshGenerator generator)
	{
		final Mesh m = generateRawMesh(world,generator);
		return buildNavigationMesh(m);
	}

	public Mesh generateRawMesh(final Node world)
	{
		final NavMeshGenerator generator = new NavMeshGenerator();
		generator.setCellSize(0.15f);
		generator.setCellHeight(0.2f);
		generator.setMinTraversableHeight(0.3f);
		generator.setMaxTraversableStep(0.25f);
		generator.setMaxTraversableSlope(60f);
		generator.setClipLedges(true);
		generator.setTraversableAreaBorderSize(0.35f);
		generator.setMinUnconnectedRegionSize(3);
		generator.setMergeRegionSize(1024);
		generator.setMaxEdgeLength(0);
		generator.setEdgeMaxDeviation(0.2f);
		generator.setMaxVertsPerPoly(16);
		generator.setContourSampleDistance(2.5f);
		generator.setContourMaxDeviation(2.5f);

		generator.setSmoothingThreshold(2);
		generator.setUseConservativeExpansion(true);
		return generateRawMesh(world,generator);
	}
	public Mesh generateRawMesh(final Node world,NavMeshGenerator generator)
	{
		final NavMeshController controller = new NavMeshController(world);
		return controller.generateNavMesh(generator);
	}
	@Override
	public NavigationMesh<ICell> buildNavigationMesh(final Mesh mesh)
	{
		// Not an improvement, really
		// return new ImprovedNavMesh(mesh);
		return new NavMesh(mesh);
	}

	@Override
	public NavigationMeshPathFinder<ICell> newNavMeshPathFinder(
			final NavigationMesh<ICell> nm)
	{
		return new DefaultNavigationMeshPathFinder<>(nm);
	}

}
