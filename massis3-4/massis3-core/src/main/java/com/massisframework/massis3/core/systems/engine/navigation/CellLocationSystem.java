package com.massisframework.massis3.core.systems.engine.navigation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.util.TempVars;
import com.massisframework.massis3.commons.app.system.AbstractMassisSystem;
import com.massisframework.massis3.commons.app.system.RequiresSystems;
import com.massisframework.massis3.commons.pathfinding.navmesh.TriangleNavigationMeshProcessor;
import com.massisframework.massis3.core.components.Position;
import com.massisframework.massis3.core.systems.required.EntityDataSystem;
import com.massisframework.massis3.simulation.ecs.EntityComponentAccessor;
import com.massisframework.massis3.simulation.ecs.EntityComponentModifier;
import com.massisframework.massis3.simulation.ecs.GeneratesComponents;
import com.massisframework.massis3.simulation.ecs.TracksComponents;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;

@GeneratesComponents(CellLocation.class)
@TracksComponents(Position.class)
@RequiresSystems({
		EntityDataSystem.class,
		NavmeshHolderSystem.class
})
public class CellLocationSystem extends AbstractMassisSystem {

	private EntitySet entities;

	private static final Logger log = LoggerFactory.getLogger(CellLocationSystem.class);
	private Map<EntityId, Integer> lastCheckedCells;

	private EntityComponentAccessor eqs;
	private EntityComponentModifier eds;
	private TriangleNavigationMeshProcessor nms;

	@Override
	public void simpleInitialize()
	{
		this.eqs = getState(EntityDataSystem.class).createAccessorFor(this);
		this.eds = getState(EntityDataSystem.class).createModifierFor(this);
		this.nms = getState(NavmeshHolderSystem.class).getTriNavMesh();

		this.lastCheckedCells = new HashMap<>();
		this.entities = eqs.getEntities(Position.class);
		this.entities.applyChanges();
		this.updateEntities(this.entities);
	}

	public Vector3f getRandomCellCenter()
	{
		return this.nms.getCellCenter(FastMath.nextRandomInt(0, this.nms.getNumCells() - 1));
	}

	@Override
	public void update()
	{
		this.refreshEntities();
	}

	private void refreshEntities()
	{
		if (this.entities.applyChanges())
		{
			this.updateEntities(this.entities.getAddedEntities());
			this.updateEntities(this.entities.getChangedEntities());
			this.entities.getRemovedEntities()
					.forEach(e -> this.lastCheckedCells.remove(e.getId()));
		}
	}

	private void updateEntities(final Set<Entity> entities)
	{
		for (final Entity e : entities)
		{
			final TempVars tmp = TempVars.get();
			final Position pos = eqs.get(e, Position.class);
			final int lastChecked = this.lastCheckedCells
					.getOrDefault(e.getId(), -1);
			final int newCell = nms.findNearestCell(pos.get(tmp.vect1),
					lastChecked);
			// TODO review this code. Is really needed?
			// The value of newCell can be null if NavMeshHolder is has not been
			// configured
			if (newCell >= 0)
			{
				if (lastChecked != newCell)
				{
					this.lastCheckedCells.put(e.getId(), newCell);
					final Vector3f pointInCell = new Vector3f();
					nms.snapPointToCell(pos.get(new Vector3f()), newCell,
							pointInCell);
					eds.setComponent(e.getId(),
							new CellLocation(newCell, true, pointInCell));
				}
			} else
			{
				if (log.isWarnEnabled())
				{
					log.warn(
							"Cell could not be located! nearestCell value: ");
				}
				this.lastCheckedCells.remove(e.getId());
				eds.setComponent(e.getId(),
						new CellLocation(-1, false, null));

			}
			tmp.release();
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
