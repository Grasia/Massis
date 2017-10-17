package com.massisframework.massis3.core.systems.engine.navigation;

import static com.massisframework.massis3.commons.pathfinding.navmesh.NavMeshCellFinderImpl.closesPointOnTriangle;
import static com.massisframework.massis3.commons.spatials.LinesUtils.closestPointOnLineSegment;
import static com.massisframework.massis3.commons.spatials.LinesUtils.direction;
import static com.massisframework.massis3.commons.spatials.LinesUtils.distance2D;
import static com.massisframework.massis3.commons.spatials.LinesUtils.length2D;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.ai.navmesh.ICell;
import com.jme3.ai.navmesh.Line2D;
import com.jme3.math.Vector3f;
import com.massisframework.massis3.commons.app.system.AbstractMassisSystem;
import com.massisframework.massis3.commons.app.system.RequiresSystems;
import com.massisframework.massis3.commons.pathfinding.navmesh.NavigationMeshProcessor;
import com.massisframework.massis3.commons.spatials.LinesUtils;
import com.massisframework.massis3.core.components.DesiredDirection;
import com.massisframework.massis3.core.components.ExtentsComponent;
import com.massisframework.massis3.core.components.FollowingEntity;
import com.massisframework.massis3.core.components.PathInfo;
import com.massisframework.massis3.core.components.Position;
import com.massisframework.massis3.core.components.Speed;
import com.massisframework.massis3.core.systems.engine.navigation.steering.AbstractDirectionSystem;
import com.massisframework.massis3.core.systems.required.EntityDataSystem;
import com.massisframework.massis3.simulation.ecs.EntityComponentAccessor;
import com.massisframework.massis3.simulation.ecs.EntityComponentModifier;
import com.massisframework.massis3.simulation.ecs.GeneratesComponents;
import com.massisframework.massis3.simulation.ecs.RemovesComponents;
import com.massisframework.massis3.simulation.ecs.TracksComponents;
import com.simsilica.es.Entity;
import com.simsilica.es.EntitySet;

/**
 * 
 * @author rpax
 *
 */
@TracksComponents({
		Position.class,
		CellLocation.class,
		FollowingEntity.class,
		Speed.class,
		PathInfo.class,
		DesiredDirection.class,
		ExtentsComponent.class
})
@GeneratesComponents({
		DesiredDirection.class
})
@RemovesComponents({
		DesiredDirection.class
})
@RequiresSystems({
		EntityDataSystem.class,
		NavmeshHolderSystem.class
})
public class CellPathDirectionSystem extends AbstractDirectionSystem {

	private static final Logger log = LoggerFactory.getLogger(CellPathDirectionSystem.class);
	private EntitySet entities;

	private EntityComponentModifier eds;
	private EntityComponentAccessor eqs;

	@Override
	public void simpleInitialize()
	{
		super.simpleInitialize();
		this.eqs = getState(EntityDataSystem.class).createAccessorFor(this);
		this.eds = getState(EntityDataSystem.class).createModifierFor(this);

		this.entities = eqs.getEntities(
				Position.class,
				ExtentsComponent.class,
				PathInfo.class,
				CellLocation.class,
				FollowingEntity.class,
				Speed.class);

	}

	@Override
	public void update()
	{
		if (this.entities.applyChanges())
		{
			this.removeEntities(this.entities.getRemovedEntities());
			this.entities.getAddedEntities()
					.forEach(e -> this.calculateNewTarget(e, tpf()));
			this.entities.getChangedEntities()
					.forEach(e -> this.calculateNewTarget(e, tpf()));
		}

	}

	public float minRad(final ICell c)
	{
		return minDistToSide(c, c.getCenter());
	}

	public float minDistToSide(final ICell c, final Vector3f p)
	{

		final double d1 = pointToLineDistance(c.getWall(0), p);
		final double d2 = pointToLineDistance(c.getWall(1), p);
		final double d3 = pointToLineDistance(c.getWall(2), p);
		return (float) Math.min(Math.min(d1, d2), d3);
	}

	public double pointToLineDistance(final Line2D l2d, final Vector3f p)
	{
		return java.awt.geom.Line2D.ptLineDist(l2d.getPointA().x,
				l2d.getPointA().y, l2d.getPointB().x, l2d.getPointB().y, p.x,
				p.z);
	}

	private void removeEntities(final Set<Entity> removedEntities)
	{
		for (final Entity e : removedEntities)
		{
			eds.remove(e.getId(), DesiredDirection.class);
		}
	}

	// -------------------------------------------------------------------------
	private Vector3f getCurrentSegmentStart(final PathInfo pI)
	{
		return pI.getMidPointPath().get(pI.getCellIndex());
	}

	public Vector3f getCurrentSegmentEnd(final PathInfo pI)
	{
		return pI.getMidPointPath().get(pI.getCellIndex() + 1);
	}

	private void calculateNewTarget(final Entity e, final float tpf)
	{

		/*
		 * Retrieve components
		 */
		/**
		 * @formatter:off
		 */
		final ExtentsComponent exc	   = eqs.get(e,ExtentsComponent.class);
		final float entityRadius         = Math.max(exc.getX(), exc.getZ());
		final Vector3f currentLocation   = eqs.get(e, Position.class).get();
		final PathInfo entityPathInfo	   = eqs.get(e, PathInfo.class);
//
		if (entityPathInfo.getMidPointPath().isEmpty()) return;
		final Vector3f segmentPointA = getCurrentSegmentStart(entityPathInfo);
		final Vector3f segmentPointB = getCurrentSegmentEnd(entityPathInfo);
		
		final DesiredDirection dd	   	   = eqs.get(e, DesiredDirection.class);
		final CellLocation entityCellLoc = eqs.get(e, CellLocation.class);
		
//		final FollowingEntity followingEntity = eqs.get(e, FollowingEntity.class);
//		final EntityId targetEntityId = followingEntity.getTarget();
//		final Position targetPosition = eqs.get(targetEntityId, Position.class);
		
		/**
		 * @formatter:on
		 */

		final Speed speed = eqs.get(e, Speed.class);
		final Vector3f closestPoint = closestPointOnLineSegment(
				segmentPointA,
				segmentPointB,
				currentLocation);

		final Vector3f nextTarget = new Vector3f();
		final Vector3f nearestObstaclePoint = nms
				.nearestObstaclePoint(currentLocation,
						entityCellLoc.getCellId());
		final float distToObstacle = LinesUtils
				.velocityDistance(currentLocation, speed.getValue(),
						nearestObstaclePoint, tpf);

		/**
		 * @formatter:off
		 */
		final Vector3f velocityVector = new Vector3f();
		if (dd!=null)
		{
			
			velocityVector.set(dd.get()).multLocal(speed.getValue());
		}
		final Vector3f straightDir = direction(currentLocation, segmentPointB);
		final Vector3f segmentDir  = direction(segmentPointA, segmentPointB);
		final Vector3f velocityDir = velocityVector.normalize();
		final float distToSegment = distance2D(currentLocation,closestPoint);
		final Vector3f[] cellTriangle = nms.getTriangle(entityCellLoc.getCellId());
		final Vector3f closestPointToCell = closesPointOnTriangle(cellTriangle,currentLocation);
		
		final float distanceToClosestPoint = closestPointToCell.distance(currentLocation);
		
		/**
		 * @formatter:on
		 */
		//
		if (distToObstacle <= entityRadius
				|| length2D(velocityVector) < 0.1f
				|| distanceToClosestPoint > entityRadius)
		{
			// direct
			nextTarget.set(segmentPointB);
		} else
		{

			final Vector3f segmentForce = new Vector3f()
					.addLocal(straightDir)
			// .addLocal(segmentDir)
			// .divideLocal(2)
			// .multLocal(2)
			;

			final Vector3f velocityForce = new Vector3f()
			// .addLocal(velocityDir)
			// .normalizeLocal()
			;
			// .divide(approximateEquals(distToSegment*distToSegment, 0) ? 1
			// : distToSegment*distToSegment);

			final Vector3f combinedForce = segmentForce
					.add(velocityForce)
					.normalizeLocal();
			// nextTarget.set(currentLocation.add(combinedForce));
			currentLocation.add(combinedForce, nextTarget);
			if (!Vector3f.isValidVector(nextTarget))
			{
				if (log.isErrorEnabled())
				{
					log.error("next target not valid : \n" +
							"\tcurrentLocation: " + currentLocation + "\n" +
							// "\tmidPointPath: " + midPointsPath + "\n" +
							"\tclosestPoint: " + closestPoint + "\n" +
							"\tdistToSegment: " + distToSegment + "\n" +
							"\tsegmentDir: " + segmentDir + "\n" +
							"\tstraightDir: " + straightDir + "\n" +
							"\tsegmentForce: " + segmentForce + "\n" +
							"\tvelocityForce: " + velocityForce + "\n" +
							"\tvelocityDir: " + velocityDir + "\n" +
							"\tentityVelocity: " + velocityVector + "\n" +
							"\tcombinedForce" + combinedForce + "\n");
				}
				nextTarget.set(segmentPointB);
			}
		}
		// final PathInfo pI = eqs.get(e, PathInfo.class);
		// if (pI.getEnd() == eqs.get(e, CellLocation.class).get())
		// {
		// eds.setComponent(e, new DesiredDirection(
		// targetPosition.getX() - currentLocation.x,
		// targetPosition.getZ() - currentLocation.z));
		// } else
		{
			eds.setComponent(e,
					new DesiredDirection()
							.withX(nextTarget.getX() - currentLocation.x)
							.withY(0)
							.withZ(nextTarget.getZ() - currentLocation.z));

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
		// TODO

	}

	@Override
	protected void onEnable()
	{
		// TODO
	}

}
