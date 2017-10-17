package com.massisframework.massis3.core.systems.required;

import java.util.Collection;
import java.util.List;

import com.jme3.animation.Animation;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.material.Material;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.massisframework.massis3.commons.app.system.AbstractMassisSystem;
import com.massisframework.massis3.commons.loader.LocalMassisSceneLoader;
import com.massisframework.massis3.commons.loader.MassisSceneLoader;
import com.massisframework.massis3.commons.loader.sh3d.xml.BuildingElementInfo;
import com.massisframework.massis3.commons.loader.sh3d.xml.FurnitureObjectInfo;
import com.massisframework.massis3.commons.loader.sh3d.xml.RoomObjectInfo;
import com.massisframework.massis3.commons.loader.sh3d.xml.WallObjectInfo;
import com.massisframework.massis3.commons.pathfinding.UniformGridGraph;
import com.massisframework.massis3.commons.pathfinding.navmesh.TriangleNavigationMeshProcessor;

public class SceneLoaderSystem extends AbstractMassisSystem {

	private final MassisSceneLoader loader;

	public SceneLoaderSystem(final MassisSceneLoader loader)
	{
		this.loader = loader;
	}

	public SceneLoaderSystem(
			List<String> assetFolders,
			String simulationSceneFile)
	{
		this.loader = new LocalMassisSceneLoader(assetFolders, simulationSceneFile);
	}

	@Override
	protected void simpleInitialize()
	{

	}

	@Override
	protected void simpleCleanup()
	{

	}

	@Override
	public void update()
	{

	}

	public Collection<CollisionShape> loadCollisionShapes()
	{
		return loader.loadCollisionShapes();
	}

	public Mesh loadRawNavMesh()
	{
		return loader.loadRawNavMesh();
	}

	public List<RoomObjectInfo> getRooms()
	{
		return loader.getRooms();
	}

	public List<FurnitureObjectInfo> getFurniture()
	{
		return loader.getFurniture();
	}

	public List<WallObjectInfo> getWalls()
	{
		return loader.getWalls();
	}

	public Material loadMaterial(final String key, final boolean fromScene)
	{
		return loader.loadMaterial(key, fromScene);
	}

	public Animation loadAnimation(final String animName)
	{
		return loader.loadAnimation(animName);
	}

	public Spatial loadSpatial(final BuildingElementInfo el)
	{
		return loader.loadSpatial(el);
	}

	public Spatial loadSpatial(final String spKey, final boolean fromScene)
	{
		return loader.loadSpatial(spKey, fromScene);
	}

	public String getSimulationSceneFile()
	{
		return loader.getSimulationSceneFile();
	}

	public String getStaticStructureId()
	{
		return loader.getStaticStructureId();
	}

	// public MassisSceneLoader getLoader()
	// {
	// return loader;
	// }

	@Override
	protected void onDisable()
	{

	}

	@Override
	protected void onEnable()
	{

	}

	public UniformGridGraph loadUniformGridGraph()
	{
		return loader.loadUniformGridGraph();
	}

	public TriangleNavigationMeshProcessor loadTriangleNavMeshProcessor()
	{
		return new TriangleNavigationMeshProcessor(loader);
	}

}
