package com.massisframework.massis3.simulation.ecs;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import com.simsilica.es.ComponentFilter;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityComponent;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;
import com.simsilica.es.WatchedEntity;

@SuppressWarnings("rawtypes")
public class ECSEnforcedEntityQueryAccessor implements EntityComponentAccessor {

	private final EntityData ed;
	private final Class caller;
	private Set<Class> allowedComponents;

	public ECSEnforcedEntityQueryAccessor(final Class caller,
			final EntityData ed)
	{
		this.ed = ed;
		this.caller = caller;
		if (this.caller != null)
		{
			this.allowedComponents = Arrays.stream(ECSAnnotationChecker
					.getAllowedComponents(caller, TracksComponents.class))
					.collect(Collectors.toSet());
		}
	}

	@Override
	public <T extends EntityComponent> T get(final Entity e,
			final Class<T> type)
	{
		this.checkAllowed(type);
		T cmp = e.get(type);
		if (cmp == null)
		{
			cmp = this.ed.getComponent(e.getId(), type);
		}
		return cmp;
	}

	@Override
	public <T extends EntityComponent> T get(final EntityId id,
			final Class<T> type)
	{
		this.checkAllowed(type);
		return this.ed.getComponent(id, type);
	}

	@Override
	public Entity getEntity(final EntityId entityId, final Class... types)
	{
		this.checkAllowed(types);
		return this.ed.getEntity(entityId, types);
	}

	@Override
	public EntityId findEntity(final ComponentFilter filter,
			final Class... types)
	{
		this.checkAllowed(types);
		return this.ed.findEntity(filter, types);
	}

	@Override
	public Set<EntityId> findEntities(final ComponentFilter filter,
			final Class... types)
	{
		this.checkAllowed(types);
		return this.ed.findEntities(filter, types);
	}

	@Override
	public EntitySet getEntities(final Class... types)
	{
		this.checkAllowed(types);
		return this.ed.getEntities(types);
	}

	@Override
	public EntitySet getEntities(final ComponentFilter filter,
			final Class... types)
	{
		this.checkAllowed(types);
		return this.ed.getEntities(filter, types);
	}

	@Override
	public WatchedEntity watchEntity(final EntityId entityId,
			final Class... types)
	{
		// this.checkAllowed(types);
		return this.ed.watchEntity(entityId, types);
	}

	private void checkAllowed(final Class... types)
	{
		if (this.caller == null)
			return;
		for (int i = 0; i < types.length; i++)
		{
			if (!this.allowedComponents.contains(types[i]))
			{
				throw new IllegalArgumentException("Type "
						+ this.caller.getSimpleName()
						+ " is not allowed to access components of type "
						+ types[i].getName());
			}
		}
	}

}
