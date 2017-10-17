package com.massisframework.massis3.commons.loader.sh3d.xml;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.material.Material;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.system.JmeSystem;
import com.massisframework.massis3.commons.loader.JsonLoader;
import com.massisframework.massis3.commons.pathfinding.UniformGridGraph;

public class MassisBuildingLoader {

	public static HomeJmeBuilding loadSpatials(final File zipFile)
	{
		Gson gson = new Gson();
		final AssetManager assetManager = JmeSystem
				.newAssetManager(JmeSystem.getPlatformAssetConfigURL());
		assetManager.registerLoader(JsonLoader.class, "json", "massis");
		assetManager.registerLocator(zipFile.getAbsolutePath(),
				ZipLocator.class);
		// TODO search for .massis file
		final JsonElement e = (JsonElement) assetManager
				.loadAsset("scene.massis");
		final HomeJmeBuilding building = gson.fromJson(e,
				HomeJmeBuilding.class);
		final Spatial models = assetManager.loadModel(building.getModelsFile());
		final Map<String, Spatial> modelsMap = new HashMap<>();
		models.depthFirstTraversal(s -> {
			modelsMap.put(s.getName(), s);
		});
		building.setSpatialLoader(key -> {
			return modelsMap.get(key).clone();
		});
		building.setMatLoader(materialId -> {
			final Path materialPath = Paths.get(building.getMaterialsFolder(),
					String.valueOf(materialId) + ".j3m");
			final Material mat = assetManager
					.loadMaterial(materialPath.toString());
			return mat;
		});
		building.setStaticCollisionShapeLoader(() -> {
			final Spatial node = assetManager
					.loadModel(building.getStaticCollisionShapeFile());
			return node.getUserDataKeys().stream().map(node::getUserData)
					.map(CollisionShape.class::cast)
					.collect(Collectors.toList());
		});

		// load NavMesh
		final String nmFile = building.getNavMeshFile();
		building.setNavMeshLoader(() -> (Mesh) assetManager.loadAsset(nmFile));
		building.setUniformGridGraphLoader(() -> {
			String file = building.getUniformGridFile();
			JsonObject obj = (JsonObject) assetManager.loadAsset(file);
			return gson.fromJson(obj, UniformGridGraph.class);
		});
		return building;
	}

	public static Node loadAllSpatialsNoBatch(
			final HomeJmeBuilding building,
			final Map<String, Spatial> modelsMap,
			final List<Material> materials)
	{
		final Node spatials = new Node();
		for (final FurnitureObjectInfo f : building.getFurniture())
		{
			final String spatialId = f.getSpatialId();
			final Spatial sp = modelsMap.get(spatialId).clone();
			sp.setLocalTranslation(f.getTranslation());
			sp.setLocalRotation(f.getRotation());
			sp.setLocalScale(f.getScale());
			sp.depthFirstTraversal(s -> {
				final Integer materialId = f.getMaterials().get(s.getName());
				if (materialId != null)
				{
					if (materialId < materials.size())
					{
						s.setMaterial(materials.get(materialId));
					}
				}
			});
			spatials.attachChild(sp);
		}
		for (final RoomObjectInfo r : building.getRooms())
		{
			// r.setSpatial(modelsMap.get(r.getSpatialId()));
			Spatial sp = modelsMap.get(r.getSpatialId());
			/*
			 * If the room has no spatial attached, it is an invisible area
			 */
			if (sp == null)
				sp = new Node();
			spatials.attachChild(sp);
		}
		for (final WallObjectInfo w : building.getWalls())
		{
			spatials.attachChild(modelsMap.get(w.getSpatialId()));
		}
		return spatials;
	}

}
