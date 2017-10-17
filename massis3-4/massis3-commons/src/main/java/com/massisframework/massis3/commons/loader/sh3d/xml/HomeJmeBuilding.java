package com.massisframework.massis3.commons.loader.sh3d.xml;

import java.util.List;
import java.util.function.Supplier;

import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.scene.Mesh;
import com.massisframework.massis3.commons.pathfinding.UniformGridGraph;

public class HomeJmeBuilding {

	public static String MASSIS_SCENE_EXTENSION = "massisscene";

	protected String version;
	protected String name;
	protected String sha1;
	protected String modelsFile;
	protected String materialsFolder;
	protected String texturesFolder;
	protected List<RoomObjectInfo> rooms;
	protected List<WallObjectInfo> walls;
	protected List<FurnitureObjectInfo> furniture;
	private final String descriptionFile;
	private final String navMeshFile;
	private final String uniformGridFile;
	private final String collisionShapeFile;

	private transient SceneMaterialLoader matLoader;
	private transient Supplier<Mesh> navMeshLoader;
	private transient SceneSpatialLoader spatialLoader;
	private transient Supplier<List<CollisionShape>> staticCollisionShapeLoader;
	private transient Supplier<UniformGridGraph> uniformGridLoader;
	private final String staticStructureId;

	public HomeJmeBuilding(
			final String version,
			final String name,
			final String sha1,
			final String descriptionFile,
			final String modelsFile,
			final String navMeshFile,
			final String uniformGridFile,
			final String collisionShapeFile,
			final String staticStructureId,
			final String materialsFolder,
			final String texturesFolder,
			final List<RoomObjectInfo> rooms,
			final List<WallObjectInfo> walls,
			final List<FurnitureObjectInfo> furniture)
	{
		this.version = version;
		this.staticStructureId = staticStructureId;
		this.modelsFile = modelsFile;
		this.collisionShapeFile = collisionShapeFile;
		this.materialsFolder = materialsFolder;
		this.texturesFolder = texturesFolder;
		this.navMeshFile = navMeshFile;
		this.uniformGridFile = uniformGridFile;
		this.descriptionFile = descriptionFile;
		this.name = name;
		this.sha1 = sha1;
		this.rooms = rooms;
		this.walls = walls;
		this.furniture = furniture;

	}

	public String getVersion()
	{
		return version;
	}

	public String getName()
	{
		return name;
	}

	public String getSha1()
	{
		return sha1;
	}

	public List<RoomObjectInfo> getRooms()
	{
		return rooms;
	}

	public List<WallObjectInfo> getWalls()
	{
		return walls;
	}

	public List<FurnitureObjectInfo> getFurniture()
	{
		return furniture;
	}

	public String getModelsFile()
	{
		return modelsFile;
	}

	public String getMaterialsFolder()
	{
		return materialsFolder;
	}

	public String getTexturesFolder()
	{
		return texturesFolder;
	}

	public String getDescriptionFile()
	{
		return this.descriptionFile;
	}

	public String getNavMeshFile()
	{
		return this.navMeshFile;
	}

	public String getUniformGridFile()
	{
		return this.uniformGridFile;
	}

	public SceneMaterialLoader getMatLoader()
	{
		return matLoader;
	}

	public void setMatLoader(final SceneMaterialLoader matLoader)
	{
		this.matLoader = matLoader;
	}

	public Supplier<Mesh> getNavMeshLoader()
	{
		return navMeshLoader;
	}

	public void setNavMeshLoader(final Supplier<Mesh> navMeshLoader)
	{
		this.navMeshLoader = navMeshLoader;
	}

	public SceneSpatialLoader getSpatialLoader()
	{
		return spatialLoader;
	}

	public void setSpatialLoader(final SceneSpatialLoader spatialLoader)
	{
		this.spatialLoader = spatialLoader;
	}

	public void setStaticCollisionShapeLoader(
			final Supplier<List<CollisionShape>> staticSceneLoader)
	{
		this.staticCollisionShapeLoader = staticSceneLoader;
	}

	public String getStaticCollisionShapeFile()
	{
		return this.collisionShapeFile;
	}

	public Supplier<List<CollisionShape>> getStaticCollisionShapeLoader()
	{
		return staticCollisionShapeLoader;
	}

	public String getStaticStructureId()
	{
		return staticStructureId;
	}

	public void setUniformGridGraphLoader(final Supplier<UniformGridGraph> uggLoader)
	{
		this.uniformGridLoader = uggLoader;

	}

	public Supplier<UniformGridGraph> getUniformGridLoader()
	{
		return uniformGridLoader;
	}

}
