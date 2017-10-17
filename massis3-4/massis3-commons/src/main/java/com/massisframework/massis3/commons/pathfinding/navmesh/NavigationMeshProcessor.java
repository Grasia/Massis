package com.massisframework.massis3.commons.pathfinding.navmesh;


import java.util.List;

import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;

public interface NavigationMeshProcessor {

	boolean intersects(int cellId, Vector3f point, float r);

	boolean contains(int cellId, Vector3f point);

	boolean intersectsCircle(int cellId, Vector3f point, float r);

	public Mesh getUnderlayingMesh();

	FindPathResult findPath(
			int start,
			int goal,
			int pathMaxLength,
			List<Integer> store);

	int findNearestCell(Vector3f location, int lastChecked);

	int findNearestCell(Vector3f location);

	Vector3f snapPointToCell(Vector3f point, Integer cellId,
			Vector3f store);

	Vector3f nearestObstaclePoint(Vector3f point, int cellIdHint);

	Vector3f getCellCenter(int cellId);

	Vector3f[] getTriangle(int cellId);

	boolean isInPath(Iterable<Integer> generatedPath, Vector3f position,
			float radius);

	public Vector3f getWorldMax();

	public Vector3f getWorldMin();

	public float distanceToNearestCellNeighbor(int cellId);

	public Vector3f getRandomPointInCell(int cellId);
	
	public int getNumCells();
}
