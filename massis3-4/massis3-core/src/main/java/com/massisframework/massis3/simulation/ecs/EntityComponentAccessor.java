package com.massisframework.massis3.simulation.ecs;

import java.util.Set;

import com.simsilica.es.ComponentFilter;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityComponent;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;
import com.simsilica.es.WatchedEntity;

@SuppressWarnings("rawtypes")
public interface EntityComponentAccessor {
	<T extends EntityComponent> T get(Entity e,
			Class<T> type);

	<T extends EntityComponent> T get(EntityId id,
			Class<T> type);

	Entity getEntity(EntityId entityId, Class... types);

	EntityId findEntity(ComponentFilter filter, Class... types);

	Set<EntityId> findEntities(ComponentFilter filter, Class... types);

	EntitySet getEntities(Class... types);

	EntitySet getEntities(ComponentFilter filter, Class... types);

	WatchedEntity watchEntity(EntityId entityId, Class... types);
}
