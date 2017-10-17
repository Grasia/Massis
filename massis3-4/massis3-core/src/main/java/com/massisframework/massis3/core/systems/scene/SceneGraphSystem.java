package com.massisframework.massis3.core.systems.scene;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.LoggerFactory;

import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.massisframework.massis3.commons.app.system.AbstractMassisSystem;
import com.massisframework.massis3.commons.app.system.RequiresSystems;
import com.massisframework.massis3.core.assets.AssetReference;
import com.massisframework.massis3.core.assets.AssetReference.AssetType;
import com.massisframework.massis3.core.components.GeometryMaterial;
import com.massisframework.massis3.core.components.MaterialInfo;
import com.massisframework.massis3.core.components.Model3DInfo;
import com.massisframework.massis3.core.systems.required.EntityDataSystem;
import com.massisframework.massis3.core.systems.required.SceneLoaderSystem;
import com.massisframework.massis3.simulation.ecs.EntityComponentAccessor;
import com.massisframework.massis3.simulation.ecs.TracksComponents;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;

@TracksComponents({
		Model3DInfo.class,
		MaterialInfo.class
})
@RequiresSystems({
		EntityDataSystem.class, SceneLoaderSystem.class
})
public class SceneGraphSystem extends AbstractMassisSystem {

	private final Map<EntityId, Spatial> models = new HashMap<EntityId, Spatial>();
	private EntitySet entities;
	private EntitySet materialEntities;
	private EntityComponentAccessor eqs;

	@Override
	public void simpleInitialize()
	{
		this.eqs = getState(EntityDataSystem.class).createAccessorFor(this);
		this.initEntities();
	}

	private void initEntities()
	{
		if (this.entities == null)
		{
			this.entities = this.eqs.getEntities(Model3DInfo.class);
			this.materialEntities = this.eqs.getEntities(
					Model3DInfo.class,
					MaterialInfo.class);
			entities.applyChanges();
			addModels(entities);
			this.materialEntities.applyChanges();
			this.materialEntities.forEach(this::updateMaterial);
		}
	}

	@Override
	public void update()
	{
		if (entities.applyChanges())
		{
			removeModels(entities.getRemovedEntities());
			addModels(entities.getAddedEntities());
		}

		if (this.materialEntities.applyChanges())
		{
			this.materialEntities.getAddedEntities()
					.forEach(this::updateMaterial);
			this.materialEntities.getChangedEntities()
					.forEach(this::updateMaterial);
		}
	}

	public Spatial getSpatial(final EntityId entity)
	{
		// Make sure we are up to date
		// refreshModels();
		if (this.entities != null)
		{
			entities.applyChanges();
			removeModels(entities.getRemovedEntities());
			addModels(entities.getAddedEntities());
			return models.get(entity);
		} else
		{
			return null;
		}
	}

	protected Spatial createSpatial(final Entity e)
	{
		final Model3DInfo model3dInfo = e.get(Model3DInfo.class);
		final AssetReference assetRef = model3dInfo.getAssetReference();
		final String key = assetRef.getKey();
		final boolean fromScene = assetRef.getAssetType() == AssetType.SCENE;
		return this.getState(SceneLoaderSystem.class).loadSpatial(key, fromScene);
	}

	protected void addModels(final Set<Entity> set)
	{

		for (final Entity e : set)
		{
			// See if we already have one
			Spatial s = models.get(e.getId());
			if (s != null)
			{
				LoggerFactory.getLogger(getClass())
						.error("Model already exists for added entity:" + e);
				continue;
			}
			s = createSpatial(e);

			models.put(e.getId(), s);
			// updateModelSpatial(e, s);
			final Spatial s_final = s;
			this.graphicalEnqueue(systemNode -> {
				systemNode.attachChild(s_final);
			});
		}
	}

	protected void removeModels(final Set<Entity> set)
	{

		for (final Entity e : set)
		{
			final Spatial s = models.remove(e.getId());
			if (s == null)
			{
				LoggerFactory.getLogger(getClass()).error("Model not found for removed entity", e);
				continue;
			}
			this.graphicalEnqueue(systemNode -> {
				s.removeFromParent();
			});
		}
	}

	public Collection<Spatial> spatials()
	{
		return models.values();
	}

	private static void addLights(final Node rootNode)
	{

		final AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White.mult(1f));
		rootNode.addLight(al);
		final Vector3f lightDir = new Vector3f(-0.39f, -0.32f, -0.7f)
				.normalizeLocal();
		final DirectionalLight sun = new DirectionalLight();
		sun.setDirection(lightDir);
		// sun.setColor(ColorRGBA.White.mult(0.9f));
		rootNode.addLight(sun);
	}

	private void updateMaterial(final Entity e)
	{
		final Spatial sp = this.getSpatial(e.getId());
		final List<GeometryMaterial> materials = e.get(MaterialInfo.class)
				.getGeometryMaterials();
		sp.depthFirstTraversal(s -> {
			final AssetReference assetReference = materials
					.stream()
					.filter(gm -> gm.getGeometryName().equals(s.getName()))
					.map(gm -> gm.getType())
					.findAny()

					.orElse(null);
			if (assetReference != null)
			{
				final Material mat = this.getState(SceneLoaderSystem.class)
						.loadMaterial(assetReference.getKey(), assetReference
								.getAssetType() == AssetType.SCENE);
				s.setMaterial(mat);
			}
		});
	}

	@Override
	protected void onEnable()
	{
		// TODO move to another section
		this.graphicalEnqueue(systemNode -> {
			addLights(systemNode);
		});

	}

	@Override
	protected void onDisable()
	{
		this.graphicalEnqueue(systemNode -> {
			systemNode.detachAllChildren();
			if (this.entities != null)
			{
				removeModels(entities);
				this.entities.release();
				this.entities.clear();
				this.materialEntities.release();
				this.materialEntities.clear();
			}
			this.entities = null;
			this.materialEntities = null;
		});
	}

}
