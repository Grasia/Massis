package com.massisframework.massis3.commons.pathfinding.navmesh;

import com.jme3.scene.Mesh;
import com.jme3.scene.Node;

public interface NavigationMeshFactory<NM extends NavMeshNode<NM>> {

	public NavigationMesh<NM> buildNavigationMesh(Node node);

	public NavigationMesh<NM> buildNavigationMesh(Mesh mesh);

	public NavigationMeshPathFinder<NM> newNavMeshPathFinder(
			NavigationMesh<NM> nm);

}
