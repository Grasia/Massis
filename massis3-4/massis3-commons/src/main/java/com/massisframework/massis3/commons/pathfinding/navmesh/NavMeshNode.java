package com.massisframework.massis3.commons.pathfinding.navmesh;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;

public interface NavMeshNode<NM extends NavMeshNode<NM>> {

	NM getLink(int side);

	/**
	 * Test to see if a 2D point is within the cell. There are probably better
	 * ways to do this, but this seems plenty fast for the time being.
	 *
	 * @param point
	 * @return
	 */
	boolean contains(Vector2f point);

	/**
	 * Test to see if a 3D point is within the cell by projecting it down to 2D
	 * and calling the above method.
	 * 
	 * @param point
	 * @return
	 */
	boolean contains(Vector3f point);

	/**
	 * Test to see if a 3D point is within the cell by projecting it down to 2D
	 * and calling the above method.
	 * 
	 * @param point
	 * @return
	 */
	boolean intersectsCircle(Vector3f point, float radius);

	Vector3f getCenter();

	public int getNumLinks();

	// public default Iterable<NM> getLinks()
	// {
	// return new Iterable<NM>() {
	//
	// @Override
	// public Iterator<NM> iterator()
	// {
	// return new Iterator<NM>() {
	// int currentIndex = 0;
	// NM current = null;
	//
	// @Override
	// public boolean hasNext()
	// {
	// skipNulls();
	// return this.currentIndex < getNumLinks();
	// }
	//
	// @Override
	// public NM next()
	// {
	// skipNulls();
	// currentIndex++;
	// return current;
	// }
	//
	// private void skipNulls()
	// {
	// while (current == null && currentIndex < getNumLinks())
	// {
	// current = getLink(currentIndex);
	// currentIndex++;
	// }
	// }
	// };
	// }
	// };
	// }

	/**
	 * Force a point to be inside the cell
	 */
	Vector3f snapPoint(Vector3f point);

}
