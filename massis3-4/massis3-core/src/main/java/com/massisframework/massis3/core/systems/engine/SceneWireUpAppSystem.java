package com.massisframework.massis3.core.systems.engine;

import java.util.List;
import java.util.stream.Collectors;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.massisframework.massis3.commons.app.system.AbstractMassisSystem;
import com.massisframework.massis3.commons.app.system.RequiresSystems;
import com.massisframework.massis3.commons.loader.sh3d.xml.FurnitureObjectInfo;
import com.massisframework.massis3.commons.loader.sh3d.xml.RoomObjectInfo;
import com.massisframework.massis3.commons.loader.sh3d.xml.WallObjectInfo;
import com.massisframework.massis3.core.assets.AssetReference;
import com.massisframework.massis3.core.assets.AssetReference.AssetType;
import com.massisframework.massis3.core.components.Facing;
import com.massisframework.massis3.core.components.FurnitureComponent;
import com.massisframework.massis3.core.components.GIDComponent;
import com.massisframework.massis3.core.components.GeometryMaterial;
import com.massisframework.massis3.core.components.LevelComponent;
import com.massisframework.massis3.core.components.MaterialInfo;
import com.massisframework.massis3.core.components.Model3DInfo;
import com.massisframework.massis3.core.components.NameComponent;
import com.massisframework.massis3.core.components.Position;
import com.massisframework.massis3.core.components.RoomComponent;
import com.massisframework.massis3.core.components.Scale;
import com.massisframework.massis3.core.components.StaticStructureComponent;
import com.massisframework.massis3.core.components.WallComponent;
import com.massisframework.massis3.core.systems.required.EntityDataSystem;
import com.massisframework.massis3.core.systems.required.SceneLoaderSystem;
import com.massisframework.massis3.simulation.ecs.EntityComponentModifier;
import com.massisframework.massis3.simulation.ecs.GeneratesComponents;
import com.simsilica.es.EntityId;

@GeneratesComponents({
		Facing.class,
		FurnitureComponent.class,
		NameComponent.class,
		Position.class,
		RoomComponent.class,
		Scale.class,
		StaticStructureComponent.class,
		WallComponent.class,
		LevelComponent.class,
		GIDComponent.class,
		MaterialInfo.class,
		Model3DInfo.class,
})
@RequiresSystems({
		EntityDataSystem.class,
		SceneLoaderSystem.class
})
public class SceneWireUpAppSystem extends AbstractMassisSystem {

	private EntityComponentModifier eds;

	@Override
	public void simpleInitialize()
	{
		this.eds = getState(EntityDataSystem.class).createModifierFor(this);
		this.initializeScene();

	}

	@Override
	public void update()
	{

	}

	public void initializeScene()
	{
		getState(SceneLoaderSystem.class).getFurniture().forEach(this::addFurniture);
		getState(SceneLoaderSystem.class).getRooms().forEach(this::addRoom);
		getState(SceneLoaderSystem.class).getWalls().forEach(this::addWall);
		this.loadStaticStructure();
	}

	private void loadStaticStructure()
	{
		final String structureId = getState(SceneLoaderSystem.class).getStaticStructureId();
		final EntityId id = eds.createEntity();
		eds.setComponents(id,
				new Position(0, 0, 0),
				new Facing(0, 0, 0, 1),
				new Scale(1, 1, 1),
				new Model3DInfo(
						new AssetReference(AssetType.SCENE, structureId)),
				new StaticStructureComponent(),
				new NameComponent("Static Structure"));

	}

	private void addRoom(final RoomObjectInfo r)
	{

		final EntityId id = eds.createEntity();
		eds.setComponents(id,
				new NameComponent(r.getName()),
				// new Position(f.getTranslation(), f.getRotation()),
				new Scale(1, 1, 1),
				new RoomComponent(toVector3fList(r.getPoints())),
				new GIDComponent().withValue(r.getMassisGID()),
				new LevelComponent().withLevelName(r.getLevelId())
		// new BatchedModel3DInfo(SCENE_SP_PREFIX + r.getSpatialId())
		);
	}

	private List<Vector3f> toVector3fList(final List<float[]> pts)
	{
		return pts.stream()
				.map(p -> new Vector3f(p[0], p[1], p[2]))
				.collect(Collectors.toList());
	}

	private void addWall(final WallObjectInfo w)
	{
		final EntityId id = eds.createEntity();
		eds.setComponents(id,
				new WallComponent(w.getHeightAtStart(), w.getHeightAtEnd(),
						w.getThickness(), toVector3fList(w.getPoints())),
				new Scale(1, 1, 1),
				new GIDComponent().withValue(w.getMassisGID()),
				new LevelComponent().withLevelName(w.getLevelId())
		// new BatchedModel3DInfo(SCENE_SP_PREFIX + w.getSpatialId())
		// new MaterialInfo(materials)
		);
	}

	private void addFurniture(final FurnitureObjectInfo f)
	{
		final EntityId id = eds.createEntity();
		final List<GeometryMaterial> materials = f.getMaterials().entrySet()
				.stream()
				.map(e -> new GeometryMaterial(e.getKey(),
						new AssetReference(AssetReference.AssetType.SCENE,
								String.valueOf(e.getValue()))))
				.collect(Collectors.toList());

		final Vector3f translation = f.getTranslation();
		final Quaternion rotation = f.getRotation();
		final Vector3f scale = f.getScale();
		eds.setComponents(id,
				new Position(translation.x, translation.y, translation.z),
				new Facing(rotation.getX(), rotation.getY(), rotation.getZ(),
						rotation.getW()),
				new Scale(scale.x, scale.y, scale.z),
				new Model3DInfo().withAssetReference(
						new AssetReference(AssetType.SCENE, f.getSpatialId())),
				new FurnitureComponent(f.isDoorOrWindow()),
				new GIDComponent().withValue(f.getMassisGID()),
				new LevelComponent().withLevelName(f.getLevelId()),
				new MaterialInfo().withGeometryMaterials(materials),
				new NameComponent(f.getName()));
	}

	@Override
	protected void onDisable()
	{

	}

	@Override
	protected void onEnable()
	{

	}

}
