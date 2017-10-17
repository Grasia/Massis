package com.massisframework.massis3.commons.loader;

import static com.jme3.system.JmeSystem.getPlatformAssetConfigURL;
import static com.jme3.system.JmeSystem.newAssetManager;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.animation.Animation;
import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.material.Material;
import com.jme3.math.Matrix4f;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.massisframework.massis3.commons.loader.animation.SceneAnimationLoader;
import com.massisframework.massis3.commons.loader.sh3d.xml.BuildingElementInfo;
import com.massisframework.massis3.commons.loader.sh3d.xml.FurnitureObjectInfo;
import com.massisframework.massis3.commons.loader.sh3d.xml.HomeJmeBuilding;
import com.massisframework.massis3.commons.loader.sh3d.xml.MassisBuildingLoader;
import com.massisframework.massis3.commons.loader.sh3d.xml.RoomObjectInfo;
import com.massisframework.massis3.commons.loader.sh3d.xml.WallObjectInfo;
import com.massisframework.massis3.commons.pathfinding.UniformGridGraph;
import com.massisframework.massis3.commons.spatials.Spatials;

public class LocalMassisSceneLoader implements MassisSceneLoader {

	private AssetManager assetManager;
	private List<String> assetPacks;
	private String simulationSceneFile;
	private HomeJmeBuilding scene;

	public LocalMassisSceneLoader(
			List<String> assetPacks,
			String simulationSceneFile)
	{
		this.assetPacks = new ArrayList<>(assetPacks);
		this.simulationSceneFile = simulationSceneFile;
		this.assetManager = newAssetManager(getPlatformAssetConfigURL());
		this.assetManager.registerLoader(SweetHome3DLoader.class, "sh3d");
		this.assetManager.registerLoader(SceneAnimationLoader.class, "massisanim");
		this.assetPacks.forEach(f -> {
			if (FilenameUtils.getExtension(f).equals("zip"))
			{
				this.assetManager.registerLocator(f, ZipLocator.class);
			} else
			{
				this.assetManager.registerLocator(f, FileLocator.class);
			}
		});
		if (this.simulationSceneFile.endsWith(HomeJmeBuilding.MASSIS_SCENE_EXTENSION))
		{
			this.scene = MassisBuildingLoader
					.loadSpatials(Paths.get(this.simulationSceneFile).toFile());
		} else
		{
			this.scene = (HomeJmeBuilding) this.assetManager
					.loadAsset(simulationSceneFile);
		}
	}

	@Override
	public Collection<CollisionShape> loadCollisionShapes()
	{
		return this.scene.getStaticCollisionShapeLoader().get();
	}

	@Override
	public Mesh loadRawNavMesh()
	{
		return this.scene.getNavMeshLoader().get();
	}

	@Override
	public List<RoomObjectInfo> getRooms()
	{
		return this.scene.getRooms();
	}

	@Override
	public List<FurnitureObjectInfo> getFurniture()
	{
		return this.scene.getFurniture();
	}

	@Override
	public List<WallObjectInfo> getWalls()

	{
		return this.scene.getWalls();
	}

	@Override
	public Material loadMaterial(String key, boolean fromScene)
	{
		if (fromScene)
		{
			return this.scene.getMatLoader()
					.loadMaterial(Integer.valueOf(key));
		} else
		{
			return this.assetManager.loadMaterial(key);
		}
	}

	@Override
	public Animation loadAnimation(String animName)
	{

		return (Animation) this.assetManager.loadAsset(animName);
	}

	@Override
	public Spatial loadSpatial(BuildingElementInfo el)
	{
		return this.scene.getSpatialLoader().loadSpatial(el.getSpatialId());
	}

	@Override
	public Spatial loadSpatial(String spKey, boolean fromScene)
	{
		Spatial sp = null;
		// String spKey = assetReference.getKey();
		// AssetType model3dType = assetReference.getAssetType();
		if (fromScene)
		{
			sp = this.scene.getSpatialLoader().loadSpatial(spKey);
		} else
		{
			sp = this.assetManager.loadModel(spKey);
			Spatials.stream(sp)
					.filter(Geometry.class::isInstance)
					.map(Geometry.class::cast)
					.forEach(g -> g.setMaterial(g.getMaterial()));
		}
		// Spatials.streamGeometries(sp)
		// .filter(g -> g.getMesh().getNumLodLevels() > 1)
		// .filter(g -> g.getControl(LodControl.class) == null)
		// .forEach(g -> {
		// g.addControl(new LodControl());
		// });

		// if (Spatials.getFirstControl(sp, AnimControl.class) != null)
		// {
		// sp.updateModelBound();
		// System.out.println(((BoundingBox)sp.getWorldBound()).getExtent(new
		// Vector3f()));
		// }

		return sp;
	}

	private static Transform getNormalizedTransform(Spatial node)
	{
		// Get model bounding box size
		// var modelBounds = this.getBounds(node);
		BoundingBox modelBounds = (BoundingBox) node.getWorldBound();
		// var lower = vec3.create();
		Vector3f lower = new Vector3f();

		// modelBounds.getLower(lower);
		modelBounds.getMin(lower);

		// var upper = vec3.create();
		Vector3f upper = new Vector3f();
		// modelBounds.getUpper(upper);
		modelBounds.getMax(upper);
		// Translate model to its center
		Matrix4f translation = new Matrix4f();
		translation.setTranslation(-lower.x - (upper.x - lower.x) / 2,
				-lower.y - (upper.y - lower.y) / 2,
				-lower.z - (upper.z - lower.z) / 2

		);
		// var modelTransform;
		Matrix4f modelTransform = Matrix4f.IDENTITY;

		modelTransform = translation;
		// Scale model to make it fill a 1 unit wide box
		Matrix4f scaleOneTransform = new Matrix4f();
		scaleOneTransform.setScale(new Vector3f(
				(lower.x - upper.x) / Math.max(0.001f, upper.x - lower.x),
				(lower.y - upper.y) / Math.max(0.001f, upper.y - lower.y),
				(upper.z - lower.z) / Math.max(0.001f, upper.z - lower.z)));
		scaleOneTransform.multLocal(modelTransform);
		Transform t0 = new Transform();
		t0.fromTransformMatrix(scaleOneTransform);
		return t0;
	}

	public void stop()
	{
		Logger logger = LoggerFactory.getLogger(getClass());
		if (logger.isInfoEnabled())
		{
			logger.info("Cleaning up scene loader cache");
		}
		this.assetManager.clearCache();
	}

	@Override
	public String getSimulationSceneFile()
	{
		return simulationSceneFile;
	}

	@Override
	public String getStaticStructureId()
	{
		return this.scene.getStaticStructureId();

	}

	@Override
	public UniformGridGraph loadUniformGridGraph()
	{
		return this.scene.getUniformGridLoader().get();
	}

}
