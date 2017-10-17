package com.jme3.ai.navmesh;

import java.io.IOException;

import com.jme3.ai.navmesh.Cell.ClassifyResult;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.massisframework.massis3.commons.pathfinding.navmesh.NavMeshNode;

public interface ICell extends NavMeshNode<ICell> {

	public enum PathResult {

		/**
		 * The path does not cross this cell
		 */
		NoRelationship,

		/**
		 * The path ends in this cell
		 */
		EndingCell,

		/**
		 * The path exits this cell through side X
		 */
		ExitingCell;

	};

	Vector3f[] getTriangle();

	/**
	 * Check if C is left of the line AB
	 */
	boolean isLeft(Vector3f a, Vector3f b, Vector3f c);

	/**
	 * Uses the X and Z information of the vector to calculate Y on the cell
	 * plane
	 * 
	 * @param point
	 */
	float getHeightOnCell(Vector3f point);

	/**
	 * Uses the X and Z information of the vector to calculate Y on the cell
	 * plane
	 * 
	 * @param point
	 */
	void computeHeightOnCell(Vector3f point);

	/**
	 * Test to see if a 2D point is within the cell. There are probably better
	 * ways to do this, but this seems plenty fast for the time being.
	 * 
	 * @param point_x
	 * @param point_y
	 * @return
	 */
	boolean contains(float point_x, float point_y);

	Vector3f getVertex(int Vert);

	int getIndex();

	/**
	 * Modified by rpax (default to public)
	 * 
	 * @param side
	 * @return
	 */
	// ICell getLink(int side);

	void setLink(int side, ICell link);

	public void initialize(Vector3f pointA, Vector3f pointB, Vector3f pointC,
			int index);

	float getWallLength(int side);

	Vector3f getWallMidpoint(int side);

	/**
	 * Classifies a Path in relationship to this cell. A path is represented by
	 * a 2D line where Point A is the start of the path and Point B is the
	 * desired position.
	 *
	 * If the path exits this cell on a side which is linked to another cell,
	 * that cell index is returned in the NextCell parameter and SideHit
	 * contains the side number of the wall exited through.
	 *
	 * If the path collides with a side of the cell which has no link (a solid
	 * edge), SideHit contains the side number (0-2) of the colliding wall.
	 *
	 * In either case PointOfIntersection will contain the point where the path
	 * intersected with the wall of the cell if it is provided by the caller.
	 */
	ClassifyResult classifyPathToCell(Line2D MotionPath);

	@Override
	String toString();

	Vector3f getNormal();

	Vector3f getRandomPoint();

	void write(JmeExporter e) throws IOException;

	void read(JmeImporter e) throws IOException;

	/**
	 * Return a mesh representation of this polygon (triangle)
	 */
	Mesh getDebugMesh();

	boolean forcePointToCellColumn(Vector2f point);

	boolean forcePointToCellColumn(Vector3f point);

	Line2D getWall(int side);

}
