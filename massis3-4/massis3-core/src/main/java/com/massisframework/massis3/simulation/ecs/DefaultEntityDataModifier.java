package com.massisframework.massis3.simulation.ecs;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import com.simsilica.es.Entity;
import com.simsilica.es.EntityComponent;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;

/**
 * Default implementation of {@link EntityComponentModifier}.
 * 
 * @author rpax
 *
 */
@SuppressWarnings("rawtypes")
public class DefaultEntityDataModifier
		implements EntityComponentModifier {

	private final EntityData ed;
	private final Class caller;
	private Set<Class> allowedGenerateComponents;
	private Set<Class> allowedRemoveComponents;

	public DefaultEntityDataModifier(
			final Class caller,
			final EntityData ed)
	{
		this.ed = ed;
		this.caller = caller;
		if (this.caller != null)
		{
			this.allowedGenerateComponents = Arrays.stream(ECSAnnotationChecker
					.getAllowedComponents(caller, GeneratesComponents.class))
					.collect(Collectors.toSet());
			this.allowedRemoveComponents = Arrays.stream(ECSAnnotationChecker
					.getAllowedComponents(caller, RemovesComponents.class))
					.collect(Collectors.toSet());
		}
	}

	@Override
	public EntityId createEntity()
	{
		return this.ed.createEntity();
	}

	@Override
	public void removeEntity(final EntityId entityId)
	{
		this.ed.removeEntity(entityId);
	}

	@Override
	public void setComponent(final Entity e, final EntityComponent component)
	{
		this.checkAllowed(this.allowedGenerateComponents, component);
		e.set(component);
	}

	@Override
	public void setComponent(final EntityId entityId,
			final EntityComponent component)
	{
		this.checkAllowed(this.allowedGenerateComponents, component);
		this.ed.setComponent(entityId, component);
	}

	@Override
	public void setComponents(final EntityId entityId,
			final EntityComponent... components)
	{
		this.checkAllowed(this.allowedGenerateComponents, components);
		this.ed.setComponents(entityId, components);
	}

	@Override
	public boolean remove(final Entity e,
			final Class<? extends EntityComponent> type)
	{
		this.checkAllowed(this.allowedRemoveComponents, type);
		return this.ed.removeComponent(e.getId(), type);
	}

	@Override
	public boolean remove(final EntityId entityId,
			final Class<? extends EntityComponent> type)
	{
		this.checkAllowed(this.allowedRemoveComponents, type);
		return this.ed.removeComponent(entityId, type);
	}

	private void checkAllowed(final Set<Class> allowedComponents,
			final EntityComponent... components)
	{
		if (this.caller == null)
			return;
		for (int i = 0; i < components.length; i++)
		{
			if (components[i] != null)
			{
				this.checkAllowed(allowedComponents, components[i].getClass());
			}
		}
	}

	private void checkAllowed(final Set<Class> allowedComponents,
			final Class... types)
	{
		if (this.caller == null)
			return;
		for (int i = 0; i < types.length; i++)
		{
			if (!allowedComponents.contains(types[i]))
			{
				throw new IllegalArgumentException("Type "
						+ this.caller.getSimpleName()
						+ " is not allowed to access components of type "
						+ types[i].getName());
			}
		}
	}

}
