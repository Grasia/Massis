package com.massisframework.massis3.core.systems.required;

import com.massisframework.massis3.commons.app.system.AbstractMassisSystem;
import com.massisframework.massis3.simulation.ecs.DefaultEntityDataModifier;
import com.massisframework.massis3.simulation.ecs.ECSEnforcedEntityQueryAccessor;
import com.massisframework.massis3.simulation.ecs.EntityComponentAccessor;
import com.massisframework.massis3.simulation.ecs.EntityComponentModifier;
import com.simsilica.es.EntityData;

public class EntityDataSystem extends AbstractMassisSystem {

	private final EntityData ed;

	public EntityDataSystem(final EntityData ed)
	{
		this.ed = ed;
	}

	@Override
	protected void simpleInitialize()
	{

	}

	@Override
	protected void onEnable()
	{

	}

	@Override
	protected void onDisable()
	{

	}

	@Override
	public void update()
	{

	}

	public EntityComponentModifier createModifierFor(final Object obj)
	{
		return new DefaultEntityDataModifier(obj.getClass(), this.ed);
	}

	public EntityComponentModifier createAbsoluteModifier()
	{
		return new DefaultEntityDataModifier(null, ed);
	}

	public EntityComponentAccessor createAccessorFor(final Object obj)
	{
		return new ECSEnforcedEntityQueryAccessor(obj.getClass(), ed);
	}

	public EntityComponentAccessor createAbsoluteAccessor()
	{
		return new ECSEnforcedEntityQueryAccessor(null, ed);
	}

}
