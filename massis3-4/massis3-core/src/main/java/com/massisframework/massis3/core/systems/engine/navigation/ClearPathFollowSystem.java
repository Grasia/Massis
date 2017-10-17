package com.massisframework.massis3.core.systems.engine.navigation;

import com.jme3.math.Vector3f;
import com.jme3.util.TempVars;
import com.massisframework.massis3.commons.app.system.AbstractMassisSystem;
import com.massisframework.massis3.commons.app.system.RequiresSystems;
import com.massisframework.massis3.core.components.CollisionFreeVelocity;
import com.massisframework.massis3.core.components.NearestPointOnFloor;
import com.massisframework.massis3.core.components.Position;
import com.massisframework.massis3.core.systems.required.EntityDataSystem;
import com.massisframework.massis3.simulation.ecs.EntityComponentAccessor;
import com.massisframework.massis3.simulation.ecs.EntityComponentModifier;
import com.massisframework.massis3.simulation.ecs.GeneratesComponents;
import com.massisframework.massis3.simulation.ecs.TracksComponents;
import com.simsilica.es.Entity;
import com.simsilica.es.EntitySet;

@GeneratesComponents({
		Position.class
})
@TracksComponents({
		Position.class, CollisionFreeVelocity.class, NearestPointOnFloor.class
})
@RequiresSystems({
		EntityDataSystem.class,
})
public class ClearPathFollowSystem extends AbstractMassisSystem {

	private EntitySet entities;
	private EntityComponentAccessor eqs;
	private EntityComponentModifier eds;

	@Override
	public void simpleInitialize()
	{
		this.eqs = getState(EntityDataSystem.class).createAccessorFor(this);
		this.eds = getState(EntityDataSystem.class).createModifierFor(this);

		this.entities = eqs.getEntities(Position.class, CollisionFreeVelocity.class);
	}

	public void updatePosition(final Entity e, final float tpf)
	{
		final Position p = eqs.get(e, Position.class);
		final NearestPointOnFloor npF = eqs.get(e, NearestPointOnFloor.class);
		final CollisionFreeVelocity cfv = eqs.get(e, CollisionFreeVelocity.class);
		final TempVars tmp = TempVars.get();
		final Vector3f newPos = new Vector3f(cfv.getX(), 0, cfv.getZ())
				.mult(tpf, tmp.vect1)
				.add(p.get(tmp.vect2), tmp.vect3);
		if (npF != null)
		{
			newPos.setY(npF.getY());
		}
		final Position nP = new Position(newPos.x, newPos.y, newPos.z);
		tmp.release();
		eds.setComponent(e.getId(), nP);
	}

	@Override
	public void update()
	{
		this.entities.applyChanges();
		this.entities.forEach(e -> updatePosition(e, tpf()));
	}

	@Override
	public void simpleCleanup()
	{
		// TODO Auto-generated method stub
		this.entities.release();
		this.entities.clear();
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
