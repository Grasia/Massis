package com.massisframework.massis3.commons.pathfinding.navmesh;

import java.io.IOException;

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.Savable;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;

public interface NavigationMesh<NM extends NavMeshNode<NM>> extends Savable {

	void clear();

	int getNumCells();

	NM getCell(int index);

	/**
	 * Find the closest cell on the mesh to the given point AVOID CALLING! not a
	 * fast routine!
	 */
	NM findClosestCell(Vector3f point);

	void loadFromMesh(Mesh mesh);

	@Override
	void write(JmeExporter e) throws IOException;

	@Override
	void read(JmeImporter e) throws IOException;

	public Mesh getUnderlayingMesh();

	boolean isInLineOfSight(NM StartCell, Vector3f StartPos, Vector3f EndPos);

	void removeCell(NM c);

}