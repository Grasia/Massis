package com.massisframework.massis3.core.systems.engine;

import java.util.HashMap;
import java.util.Map;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.util.TempVars;
import com.massisframework.massis3.commons.app.system.AbstractMassisSystem;
import com.massisframework.massis3.commons.app.system.RequiresSystems;
import com.massisframework.massis3.core.components.CollisionFreeVelocity;
import com.massisframework.massis3.core.components.Facing;
import com.massisframework.massis3.core.components.Position;
import com.massisframework.massis3.core.components.Speed;
import com.massisframework.massis3.core.systems.required.EntityDataSystem;
import com.massisframework.massis3.simulation.ecs.EntityComponentAccessor;
import com.massisframework.massis3.simulation.ecs.EntityComponentModifier;
import com.massisframework.massis3.simulation.ecs.GeneratesComponents;
import com.massisframework.massis3.simulation.ecs.TracksComponents;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;

@GeneratesComponents({
		Facing.class
})
@TracksComponents({
		Facing.class,
		Position.class,
		Speed.class,
		CollisionFreeVelocity.class
})
@RequiresSystems({
		EntityDataSystem.class,
})
public class VelocityRotationSystem extends AbstractMassisSystem {

	private EntitySet entities;
	private final Map<EntityId, Vector3f> lastPositions;
	private EntityComponentAccessor eqs;
	private EntityComponentModifier eds;

	private static final float LOCATION_THRESHOLD = 0.1f;

	public VelocityRotationSystem()
	{
		this.lastPositions = new HashMap<>();
	}

	@Override
	public void simpleInitialize()
	{
		this.eqs = getState(EntityDataSystem.class).createAccessorFor(this);
		this.eds = getState(EntityDataSystem.class).createModifierFor(this);
		this.entities = eqs
				.getEntities(
						Facing.class,
						Position.class,
						CollisionFreeVelocity.class);
		this.entities.applyChanges();
		this.entities.forEach(e -> this.updateRotation(e, 0f));
	}

	// TODO better implementation
	private void updateRotation(final Entity e, float tpf)
	{
		TempVars tmp = TempVars.get();
		Position position = eqs.get(e, Position.class);
		Vector3f lastLoc = this.lastPositions.get(e.getId());
		if (lastLoc == null)
		{
			lastLoc = position.get(new Vector3f());
			this.lastPositions.put(e.getId(), lastLoc);
		}
		Vector3f currentLoc = position.get(tmp.vect1);
		if (currentLoc.distance(lastLoc) > LOCATION_THRESHOLD * tpf)
		{
			Facing facing = eqs.get(e.getId(), Facing.class);
			final CollisionFreeVelocity v = eqs.get(e.getId(), CollisionFreeVelocity.class);
			final Quaternion newRot1 = tmp.quat1;
			final Quaternion interpolated = tmp.quat2;
			newRot1.lookAt(new Vector3f(v.getX(), 0, v.getZ()), Vector3f.UNIT_Y);
			facing.get(interpolated).nlerp(newRot1, 1);

			eds.setComponent(e.getId(), new Facing(
					interpolated.getX(),
					interpolated.getY(),
					interpolated.getZ(),
					interpolated.getW()));

		}

		lastLoc.set(currentLoc);
		tmp.release();
	}

	@Override
	public void update()
	{

	}

	@Override
	public void simpleCleanup()
	{
		this.entities.release();
		this.entities.clear();

	}

	@Override
	protected void onDisable()
	{

	}

	@Override
	protected void onEnable()
	{

	}

	@Override
	public void graphicalUpdate(Node systemNode)
	{
		if (this.entities.applyChanges())
		{
			this.entities.forEach(e -> this.updateRotation(e, tpf()));
		}
	}

}
