package com.massisframework.massis3.commons.loader;

import java.util.Collection;
import java.util.List;

import com.jme3.animation.Animation;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.material.Material;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.massisframework.massis3.commons.loader.sh3d.xml.BuildingElementInfo;
import com.massisframework.massis3.commons.loader.sh3d.xml.FurnitureObjectInfo;
import com.massisframework.massis3.commons.loader.sh3d.xml.RoomObjectInfo;
import com.massisframework.massis3.commons.loader.sh3d.xml.WallObjectInfo;
import com.massisframework.massis3.commons.pathfinding.UniformGridGraph;

public interface MassisSceneLoader {

	Collection<CollisionShape> loadCollisionShapes();

	Mesh loadRawNavMesh();

	List<RoomObjectInfo> getRooms();

	List<FurnitureObjectInfo> getFurniture();

	List<WallObjectInfo> getWalls();

	public Material loadMaterial(String key,boolean fromScene);

	Animation loadAnimation(String animName);

	Spatial loadSpatial(BuildingElementInfo el);

	Spatial loadSpatial(String spKey,boolean fromScene);

	String getSimulationSceneFile();

	String getStaticStructureId();

	UniformGridGraph loadUniformGridGraph();

}