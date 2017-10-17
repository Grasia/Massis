package com.massisframework.massis3.core.systems.debug;

import java.util.HashMap;
import java.util.Map;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.util.TempVars;
import com.massisframework.massis3.commons.app.system.AbstractMassisSystem;
import com.massisframework.massis3.commons.app.system.RequiresSystems;
import com.massisframework.massis3.core.components.Facing;
import com.massisframework.massis3.core.components.Human;
import com.massisframework.massis3.core.components.Position;
import com.massisframework.massis3.core.systems.required.EntityDataSystem;
import com.massisframework.massis3.core.systems.scene.SceneGraphSystem;
import com.massisframework.massis3.simulation.ecs.TracksComponents;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;

@RequiresSystems({
		Text3DManagerSystem.class,
		SceneGraphSystem.class,
		EntityDataSystem.class
})
@TracksComponents({ Human.class, Position.class, Facing.class })
public class ShowIdsDebugSystem extends AbstractMassisSystem implements DebugSystem {

	private EntitySet humans;
	private Map<EntityId, Spatial> texts;
	private Node textNode;

	@Override
	public void simpleInitialize()
	{
		this.textNode = new Node();
		this.texts = new HashMap<>();
	}

	@Override
	public void update()
	{
	}

	private void removeEntity(Entity e)
	{
		removeTextById(e.getId());
	}

	private void removeTextById(EntityId id)
	{
		Spatial text = this.texts.remove(id);
		if (text != null)
		{
			text.removeFromParent();
		}
	}

	private void updateEntity(Entity e)
	{
		TempVars tmp = TempVars.get();
		Spatial textSp = this.getText(e.getId());
		Spatial humanSp = getState(SceneGraphSystem.class).getSpatial(e.getId());
		BoundingBox humanBbox = (BoundingBox) humanSp.getWorldBound();

		Vector3f center = humanBbox.getCenter(tmp.vect1);
		center.addLocal(0, humanBbox.getYExtent(), 0);
		textSp.setLocalScale(0.36f);
		textSp.setLocalTranslation(center);
		textSp.setLocalRotation(e.get(Facing.class).get(tmp.quat1));
		tmp.release();
	}

	@Override
	protected void onEnable()
	{
		this.graphicalEnqueue(node -> {
			this.humans = getState(EntityDataSystem.class)
					.createAccessorFor(this)
					.getEntities(
							Human.class,
							Position.class,
							Facing.class);
			node.attachChild(this.textNode);
		});
	}

	private Spatial getText(EntityId eid)
	{
		Spatial text = this.texts.get(eid);
		if (text == null)
		{
			text = this.getState(Text3DManagerSystem.class)
					.loadText(String.valueOf(eid.getId()));
			this.texts.put(eid, text);
			this.textNode.attachChild(text);
		}
		return text;

	}

	@Override
	protected void onDisable()
	{
		this.humans.stream().map(Entity::getId).forEach(this::removeTextById);
		this.humans.release();
		this.humans.clear();
		this.humans = null;
	}

	@Override
	public void graphicalUpdate(Node systemNode)
	{
		if (this.humans.applyChanges())
		{
			this.humans.getAddedEntities().forEach(this::updateEntity);
			this.humans.getChangedEntities().forEach(this::updateEntity);
			this.humans.getRemovedEntities().forEach(this::removeEntity);
		}
	}

}
