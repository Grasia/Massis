package com.massisframework.massis3.simulation.ecs;

import com.simsilica.es.Entity;
import com.simsilica.es.EntityComponent;
import com.simsilica.es.EntityId;

/**
 * This service offers entity modification functionalities:
 * <ul>
 * <li>Creating entities</li>
 * <li>Removing entities</li>
 * <li>Adding components</li>
 * <li>Removing components</li>
 * </ul>
 * 
 * @author rpax
 *
 */
public interface EntityComponentModifier {

	EntityId createEntity();

	void removeEntity(EntityId entityId);

	void setComponent(Entity e, EntityComponent component);

	void setComponent(EntityId entityId, EntityComponent component);

	default void setComponent(long entityId, EntityComponent component)
	{
		this.setComponent(new EntityId(entityId), component);
	}

	void setComponents(EntityId entityId, EntityComponent... components);

	boolean remove(Entity e, Class<? extends EntityComponent> type);

	boolean remove(EntityId entityId, Class<? extends EntityComponent> type);

}