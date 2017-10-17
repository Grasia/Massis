package com.massisframework.massis3.commons.raycast;

import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;

public interface SceneRayCaster {

	void addCollisionMesh(Mesh m);

	void addCollisionShapes(Iterable<CollisionShape> cshapes);

	void addCollisionShape(CollisionShape cs);

	boolean rayCastNearestHit(
			Vector3f from,
			Vector3f to,
			Vector3f store);

	void cleanup();

}