package com.massisframework.massis3.core.systems.engine;

import com.massisframework.massis3.commons.app.system.AbstractMassisSystem;
import com.massisframework.massis3.commons.app.system.RequiresSystems;
import com.massisframework.massis3.core.components.CountDownDestroyComponent;
import com.massisframework.massis3.core.systems.required.EntityDataSystem;
import com.massisframework.massis3.simulation.ecs.EntityComponentAccessor;
import com.massisframework.massis3.simulation.ecs.EntityComponentModifier;
import com.massisframework.massis3.simulation.ecs.GeneratesComponents;
import com.massisframework.massis3.simulation.ecs.TracksComponents;
import com.simsilica.es.Entity;
import com.simsilica.es.EntitySet;

@TracksComponents({
		CountDownDestroyComponent.class
})
@GeneratesComponents({
		CountDownDestroyComponent.class
})
@RequiresSystems({
		EntityDataSystem.class,
})
public class CountDownDestroySystem extends AbstractMassisSystem {

	private EntitySet entities;

	private EntityComponentAccessor eqs;
	private EntityComponentModifier eds;

	@Override
	public void simpleInitialize()
	{
		this.eqs = getState(EntityDataSystem.class).createAccessorFor(this);
		this.eds = getState(EntityDataSystem.class).createModifierFor(this);
		this.entities = eqs
				.getEntities(CountDownDestroyComponent.class);
	}

	@Override
	public void update()
	{
		this.entities.applyChanges();
		for (final Entity e : entities)
		{
			final CountDownDestroyComponent cc = eqs.get(e,
					CountDownDestroyComponent.class);
			if (cc.getCountdown() > 0)
			{
				cc.setCountdown(cc.getCountdown() - tpf());
			}
			if (cc.getCountdown() <= 0)
			{
				eds.removeEntity(e.getId());
			} else
			{
				eds.setComponent(e, cc);
			}
		}
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
