package com.massisframework.massis3.commons.pathfinding.navmesh;

import java.util.List;

import com.jme3.math.Vector3f;

public interface NavigationMeshPathFinder<NMNode extends NavMeshNode> {

	public FindPathResult findPath(Vector3f start, Vector3f goal,int pathMaxLength,List<NMNode> store);
	public FindPathResult findPath(NMNode start, NMNode goal, int pathMaxLength,List<NMNode> store);
	
}
