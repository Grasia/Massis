package com.massisframework.massis3.core.systems.engine.physics;

import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.util.TempVars;
import com.massisframework.massis3.commons.app.system.AbstractMassisSystem;
import com.massisframework.massis3.commons.app.system.RequiresSystems;
import com.massisframework.massis3.commons.raycast.NativeRayCaster;
import com.massisframework.massis3.commons.raycast.SceneRayCaster;
import com.massisframework.massis3.core.components.ExtentsComponent;
import com.massisframework.massis3.core.components.Human;
import com.massisframework.massis3.core.components.Mass;
import com.massisframework.massis3.core.components.NearestPointOnFloor;
import com.massisframework.massis3.core.components.Position;
import com.massisframework.massis3.core.systems.engine.navigation.CellLocation;
import com.massisframework.massis3.core.systems.required.EntityDataSystem;
import com.massisframework.massis3.core.systems.required.SceneLoaderSystem;
import com.massisframework.massis3.simulation.ecs.EntityComponentAccessor;
import com.massisframework.massis3.simulation.ecs.EntityComponentModifier;
import com.massisframework.massis3.simulation.ecs.GeneratesComponents;
import com.massisframework.massis3.simulation.ecs.TracksComponents;
import com.simsilica.es.Entity;
import com.simsilica.es.EntitySet;

@GeneratesComponents({
		NearestPointOnFloor.class, Position.class
})
@TracksComponents({
		Position.class, Mass.class, Human.class,
		CellLocation.class, ExtentsComponent.class
})
@RequiresSystems({
		EntityDataSystem.class,
		SceneLoaderSystem.class
})
public class SimpleGravitySystem extends AbstractMassisSystem {

	private EntitySet entities;

	private EntityComponentAccessor eqs;

	private EntityComponentModifier eds;

	private SceneRayCaster meshRayCaster;

	private SceneRayCaster elementsRayCaster;

	private static final float DISTANCE_TO_FLOOR_EPSILON = 0.01f;
	private static final Vector3f DOWN_VECT = new Vector3f(0, -1000, 0);

	@Override
	public void simpleInitialize()
	{
		this.eqs = getState(EntityDataSystem.class).createAccessorFor(this);
		this.eds = getState(EntityDataSystem.class).createModifierFor(this);
		this.entities = eqs.getEntities(
				Position.class,
				Mass.class,
				Human.class,
				ExtentsComponent.class);
		this.entities.applyChanges();
		this.entities.forEach(e -> this.applyGravity(e, true));

	}

	private SceneRayCaster getMeshRayCaster()
	{
		if (this.meshRayCaster == null)
		{
			this.meshRayCaster = new NativeRayCaster();
			Mesh rm = getState(SceneLoaderSystem.class).loadRawNavMesh();
			this.meshRayCaster.addCollisionMesh(rm);
		}
		return this.meshRayCaster;
	}

	private SceneRayCaster getElementsRayCaster()
	{
		if (this.elementsRayCaster == null)
		{
			this.elementsRayCaster = new NativeRayCaster();
			this.elementsRayCaster
					.addCollisionShapes(getState(SceneLoaderSystem.class).loadCollisionShapes());
		}
		return this.elementsRayCaster;
	}

	private void applyGravity(final Entity e, boolean force)
	{

		final Position p = eqs.get(e, Position.class);
		final float px = p.getX();
		final float py = p.getY();
		final float pz = p.getZ();
		final CellLocation cellLocation = eqs.get(e, CellLocation.class);
		final ExtentsComponent exc = eqs.get(e, ExtentsComponent.class);
		// FIXME esto esta mal. La posicion es en el suelo!
		final Vector3f nearestPointOnFloor = new Vector3f(px,
				py - exc.getY() + DISTANCE_TO_FLOOR_EPSILON, pz);
		// Mass m = eqs.get(e, Mass.class);
		final TempVars tmp = TempVars.get();
		final Vector3f from = p.get(tmp.vect2)
				.addLocal(0, exc.getY(), 0);
		if (cellLocation != null && cellLocation.isValid())
		{
			final Vector3f nearest = cellLocation.getPointInCell();
			nearestPointOnFloor.set(nearest);
		} else
		{

			final Vector3f to = from.add(DOWN_VECT, tmp.vect4);
			final Vector3f nearest = tmp.vect5;
			final boolean hasHit = nearestHit(from, to, nearest);
			// nearest.addLocal(0, exc.getExtentY(), 0);
			if (hasHit)// && nearest.distance(from) >= exc.getExtentY()+
						// DISTANCE_TO_FLOOR_EPSILON)
			{
				nearestPointOnFloor.set(nearest);
			}
		}
		if (!force)
		{
			eds.setComponent(e.getId(),
					new NearestPointOnFloor(
							nearestPointOnFloor.x,
							nearestPointOnFloor.y,
							nearestPointOnFloor.z));
		} else
		{
			eds.setComponent(e.getId(),
					new Position(
							nearestPointOnFloor.x,
							nearestPointOnFloor.y,
							nearestPointOnFloor.z));
		}
		tmp.release();
	}

	private boolean nearestHit(final Vector3f from, final Vector3f to,
			final Vector3f store)
	{
		return getMeshRayCaster().rayCastNearestHit(from, to, store)
				|| getElementsRayCaster().rayCastNearestHit(from, to, store);
	}

	@Override
	public void update()
	{
		if (this.entities.applyChanges())
		{
			this.entities.getAddedEntities().forEach(e -> this.applyGravity(e, true));
			this.entities.getChangedEntities().forEach(e -> this.applyGravity(e, false));
		}
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

}
