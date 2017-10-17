package com.massisframework.massis3.core.systems.engine.navigation;

import com.jme3.math.Vector3f;
import com.jme3.util.TempVars;
import com.massisframework.massis3.commons.app.system.AbstractMassisSystem;
import com.massisframework.massis3.commons.app.system.RequiresSystems;
import com.massisframework.massis3.core.components.CollisionFreeVelocity;
import com.massisframework.massis3.core.components.DesiredDirection;
import com.massisframework.massis3.core.components.ExtentsComponent;
import com.massisframework.massis3.core.components.Position;
import com.massisframework.massis3.core.components.Speed;
import com.massisframework.massis3.core.systems.engine.navigation.rvo2.RVO2Component;
import com.massisframework.massis3.core.systems.engine.navigation.rvo2.RVO2DesiredDirection;
import com.massisframework.massis3.core.systems.required.EntityDataSystem;
import com.massisframework.massis3.simulation.ecs.EntityComponentAccessor;
import com.massisframework.massis3.simulation.ecs.EntityComponentModifier;
import com.massisframework.massis3.simulation.ecs.GeneratesComponents;
import com.massisframework.massis3.simulation.ecs.RemovesComponents;
import com.massisframework.massis3.simulation.ecs.TracksComponents;
import com.simsilica.es.Entity;
import com.simsilica.es.EntitySet;

@TracksComponents({
		RVO2Component.class,
		RVO2DesiredDirection.class,
		DesiredDirection.class,
		Position.class,
		Speed.class,
		ExtentsComponent.class
})
@GeneratesComponents({
		RVO2Component.class,
		CollisionFreeVelocity.class
})
@RemovesComponents({
		RVO2Component.class,
		CollisionFreeVelocity.class
})
@RequiresSystems({
		EntityDataSystem.class,
})
public class CollisionFreeVelocityGeneratorSystem extends AbstractMassisSystem {

	private EntitySet entities;
	private EntitySet rvo2Entities;

	private static float DIRECTION_MULTIPLIER = 10;

	private EntityComponentAccessor eqs;
	private EntityComponentModifier eds;

	@Override
	public void simpleInitialize()
	{
		this.eqs = getState(EntityDataSystem.class).createAccessorFor(this);
		this.eds = getState(EntityDataSystem.class).createModifierFor(this);

		this.entities = eqs.getEntities(Position.class, DesiredDirection.class,
				ExtentsComponent.class);
		this.rvo2Entities = eqs.getEntities(RVO2DesiredDirection.class);
		this.entities.applyChanges();
		this.entities.forEach(this::execUpdatePipeline);

	}

	private void updateRVO2(final Entity e)
	{
		RVO2Component rvo2c = eqs.get(e, RVO2Component.class);
		if (rvo2c == null)
		{
			rvo2c = new RVO2Component();
		}

		final DesiredDirection dd = eqs.get(e, DesiredDirection.class);
		final Position position = eqs.get(e, Position.class);
		// FIXME what about speed
		// final Speed speed = eqs.get(e, Speed.class);
		final ExtentsComponent exc = eqs.get(e, ExtentsComponent.class);
		float goalX = 0, goalZ = 0;
		final TempVars tmp = TempVars.get();
		final Vector3f lt = dd.get()
				// HARDCODED
				// .getVelocity(DIRECTION_MULTIPLIER, tmp.vect1)
				.mult(DIRECTION_MULTIPLIER, tmp.vect1)
				.addLocal(position.get());
		goalX = lt.x;
		goalZ = lt.z;
		tmp.release();
		final float radius = Math.max(exc.getX(), exc.getZ());
		rvo2c.setRadius(radius);
		rvo2c.setNeighborDistance(3);
		rvo2c.setMaxNeighbors(100);
		rvo2c.setGoalX(goalX);
		rvo2c.setGoalZ(goalZ);
		rvo2c.setTimeHorizonAgents(10);
		rvo2c.setTimeHorizonObstacles(10);
		eds.setComponent(e, rvo2c);
	}

	private void execUpdatePipeline(final Entity e)
	{
		this.updateRVO2(e);
		// Possibly more

	}

	private void execPublishPipeline(final Entity e)
	{
		final RVO2DesiredDirection d = eqs.get(e, RVO2DesiredDirection.class);
		eds.setComponent(e, new CollisionFreeVelocity(d.getX(), d.getZ()));
	}

	private void removeFromPipeline(final Entity e)
	{
		eds.remove(e, RVO2Component.class);
		eds.remove(e, CollisionFreeVelocity.class);
		// Possibly more
	}

	@Override
	public void update()
	{
		if (this.entities.applyChanges())
		{
			entities.getAddedEntities().forEach(this::execUpdatePipeline);
			entities.getChangedEntities().forEach(this::execUpdatePipeline);
			entities.getRemovedEntities().forEach(this::removeFromPipeline);
		}
		if (this.rvo2Entities.applyChanges())
		{
			this.rvo2Entities.getChangedEntities()
					.forEach(this::execPublishPipeline);
			this.rvo2Entities.getRemovedEntities()
					.forEach(this::removeFromPipeline);
		}
	}

	@Override
	public void simpleCleanup()
	{
		this.entities.release();
		this.entities.clear();
		this.rvo2Entities.release();
		this.rvo2Entities.clear();

	}

	@Override
	protected void onDisable()
	{
		// TODO

	}

	@Override
	protected void onEnable()
	{
		// TODO
	}

}
