package com.massisframework.massis3.commons.pathfinding.navmesh;

import com.jme3.ai.navmesh.ICell;
import com.jme3.math.Vector3f;

public interface NavMeshCellFinder {

	ICell findNearestCell(Vector3f point);

	ICell findNearestCell(Vector3f point, ICell lastHint);

}