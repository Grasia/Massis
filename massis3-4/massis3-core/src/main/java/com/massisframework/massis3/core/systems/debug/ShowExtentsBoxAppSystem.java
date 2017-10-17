package com.massisframework.massis3.core.systems.debug;

import java.util.HashMap;
import java.util.Map;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.util.TempVars;
import com.massisframework.massis3.commons.app.system.AbstractMassisSystem;
import com.massisframework.massis3.commons.app.system.RequiresSystems;
import com.massisframework.massis3.commons.spatials.Materials;
import com.massisframework.massis3.commons.spatials.Spatials;
import com.massisframework.massis3.core.components.BoundingBoxComponent;
import com.massisframework.massis3.core.components.ExtentsComponent;
import com.massisframework.massis3.core.components.Position;
import com.massisframework.massis3.core.systems.required.EntityDataSystem;
import com.massisframework.massis3.simulation.ecs.EntityComponentAccessor;
import com.massisframework.massis3.simulation.ecs.TracksComponents;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;

@RequiresSystems({
		EntityDataSystem.class,
})
@TracksComponents({
		Position.class,
		ExtentsComponent.class,
		BoundingBoxComponent.class
})
public class ShowExtentsBoxAppSystem extends AbstractMassisSystem implements DebugSystem {

	private EntityComponentAccessor eqs;
	private EntitySet entities;
	private Map<EntityId, Spatial> boxSpatials;
	private static final Material RED_UNSHADED = Materials.newUnshaded(ColorRGBA.Red, true);

	@Override
	protected void simpleInitialize()
	{
		this.eqs = getState(EntityDataSystem.class).createAccessorFor(this);
		this.boxSpatials = new HashMap<>();
	}

	@Override
	protected void onDisable()
	{
		this.boxSpatials.values().forEach(Spatial::removeFromParent);
		this.boxSpatials.clear();
		this.entities.release();
		this.entities = null;
	}

	private void updateEntity(Entity e, Node systemNode)
	{
		TempVars tmp = TempVars.get();
		Spatial sp = this.boxSpatials.get(e.getId());
		Vector3f loc = this.eqs.get(e, Position.class).get(tmp.vect1);
		Vector3f ex = this.eqs.get(e, ExtentsComponent.class).get(tmp.vect2);
		if (sp == null)
		{
			sp = Spatials.createBox(loc, RED_UNSHADED);
			this.boxSpatials.put(e.getId(), sp);
			systemNode.attachChild(sp);
		}
		sp.setLocalScale(ex.x, ex.y, ex.z);
		sp.setLocalTranslation(loc);
		tmp.release();
	}

	private void removeEntity(Entity e)
	{
		this.removeEntity(e.getId());
	}

	private void removeEntity(EntityId id)
	{
		Spatial sp = this.boxSpatials.remove(id);
		if (sp != null)
		{
			sp.removeFromParent();
		}
	}

	@Override
	protected void onEnable()
	{
		this.graphicalEnqueue(node -> {
			this.entities = this.eqs.getEntities(Position.class, ExtentsComponent.class,
					BoundingBoxComponent.class);
			this.entities.applyChanges();
			this.entities.forEach(e -> this.updateEntity(e, node));
		}); 
	}

	@Override
	public void update()
	{

	}

	@Override
	public void graphicalUpdate(Node systemNode)
	{
		if (this.entities.applyChanges())
		{
			this.entities.getAddedEntities().forEach(e -> this.updateEntity(e, systemNode));
			this.entities.getChangedEntities().forEach(e -> this.updateEntity(e, systemNode));
			this.entities.getRemovedEntities().forEach(e -> this.removeEntity(e));
		}

	}

}
