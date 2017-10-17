package com.massisframework.massis3.core.systems.debug;

import java.util.ArrayList;
import java.util.List;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.massisframework.massis3.commons.app.system.AbstractMassisSystem;
import com.massisframework.massis3.commons.app.system.RequiresSystems;
import com.massisframework.massis3.commons.spatials.Materials;
import com.massisframework.massis3.commons.spatials.Spatials;
import com.massisframework.massis3.core.components.LevelComponent;
import com.massisframework.massis3.core.components.NameComponent;
import com.massisframework.massis3.core.components.RoomComponent;
import com.massisframework.massis3.core.systems.required.EntityDataSystem;
import com.massisframework.massis3.simulation.ecs.EntityComponentAccessor;
import com.massisframework.massis3.simulation.ecs.TracksComponents;
import com.simsilica.es.Entity;
import com.simsilica.es.EntitySet;

@RequiresSystems({
		EntityDataSystem.class
})
@TracksComponents({
		RoomComponent.class,
		LevelComponent.class,
		NameComponent.class
})
public class ShowRoomAreasAppSystem extends AbstractMassisSystem implements DebugSystem {
	private EntitySet roomEntities;
	private EntityComponentAccessor eqs;

	@Override
	protected void simpleInitialize()
	{
		this.eqs = getState(EntityDataSystem.class).createAccessorFor(this);

	}

	@Override
	protected void onDisable()
	{
		if (this.roomEntities != null)
		{

			// this.roomEntities.forEach(this::removeRoom);
			this.roomEntities.release();
			this.roomEntities.clear();
			this.roomEntities = null;
		}
		this.graphicalEnqueue(node -> {
			node.detachAllChildren();
		});

	}

	@Override
	protected void onEnable()
	{
		this.graphicalEnqueue(node -> {
			this.roomEntities = this.eqs.getEntities(
					LevelComponent.class,
					RoomComponent.class,
					NameComponent.class);
			this.roomEntities.applyChanges();
			this.roomEntities.forEach(e -> this.addRoom(e, node));
		});
	}

	private void addRoom(Entity e, Node systemNode)
	{
		List<Vector3f> points = this.eqs.get(e, RoomComponent.class).getPoints();
		List<Vector3f[]> lines = new ArrayList<>();
		for (int i = 0; i < points.size(); i++)
		{
			lines.add(new Vector3f[] { points.get(i), points.get((i + 1) % points.size()) });
		}
		Mesh lineMesh = Spatials.createLineMesh(lines);
		Geometry g = new Geometry("Room borders", lineMesh);
		Material mat = Materials.newUnshaded(ColorRGBA.randomColor());
		mat.getAdditionalRenderState().setLineWidth(2f);
		g.setMaterial(mat);
		systemNode.attachChild(g);
	}

	@Override
	public void update()
	{

	}

	@Override
	public void graphicalUpdate(Node systemNode)
	{
		if (this.roomEntities.applyChanges())
		{
			this.roomEntities.getAddedEntities().forEach(e -> this.addRoom(e, systemNode));
		}

	}

}
