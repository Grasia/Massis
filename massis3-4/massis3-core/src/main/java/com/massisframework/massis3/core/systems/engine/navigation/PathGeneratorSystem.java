package com.massisframework.massis3.core.systems.engine.navigation;

import static com.massisframework.massis3.commons.spatials.LinesUtils.minimum_distance2D;
import static com.massisframework.massis3.commons.spatials.LinesUtils.nearestLineSegment;

import java.util.ArrayList;
import java.util.List;

import com.jme3.math.Vector3f;
import com.jme3.util.TempVars;
import com.massisframework.massis3.commons.app.system.AbstractMassisSystem;
import com.massisframework.massis3.commons.app.system.RequiresSystems;
import com.massisframework.massis3.commons.pathfinding.navmesh.NavigationMeshProcessor;
import com.massisframework.massis3.commons.pathfinding.navmesh.NavigationMeshUtil;
import com.massisframework.massis3.commons.pathfinding.navmesh.TriangleNavigationMeshProcessor;
import com.massisframework.massis3.core.components.ExtentsComponent;
import com.massisframework.massis3.core.components.FollowingEntity;
import com.massisframework.massis3.core.components.PathInfo;
import com.massisframework.massis3.core.components.PathInfo.FindPathResult;
import com.massisframework.massis3.core.systems.required.EntityDataSystem;
import com.massisframework.massis3.core.components.Position;
import com.massisframework.massis3.core.components.Speed;
import com.massisframework.massis3.core.components.TargetReached;
import com.massisframework.massis3.simulation.ecs.EntityComponentAccessor;
import com.massisframework.massis3.simulation.ecs.EntityComponentModifier;
import com.massisframework.massis3.simulation.ecs.GeneratesComponents;
import com.massisframework.massis3.simulation.ecs.RemovesComponents;
import com.massisframework.massis3.simulation.ecs.TracksComponents;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;

@TracksComponents({
		Position.class,
		ExtentsComponent.class,
		Speed.class,
		FollowingEntity.class,
		PathInfo.class,
		CellLocation.class
})
@GeneratesComponents({
		PathInfo.class,
		TargetReached.class
})
@RemovesComponents({
		PathInfo.class
})
@RequiresSystems({
		EntityDataSystem.class, NavmeshHolderSystem.class
})
public class PathGeneratorSystem extends AbstractMassisSystem {

	private EntitySet entities;
	private EntityComponentModifier eds;
	private EntityComponentAccessor eqs;
	private NavigationMeshProcessor nms;

	@Override
	public void simpleInitialize()
	{

		this.eqs = getState(EntityDataSystem.class).createAccessorFor(this);
		this.eds = getState(EntityDataSystem.class).createModifierFor(this);
		this.nms = getState(NavmeshHolderSystem.class).getTriNavMesh();

		this.entities = this.eqs
				.getEntities(
						Position.class,
						ExtentsComponent.class,
						FollowingEntity.class,
						CellLocation.class

		);

	}

	private boolean pathMustBeRecomputed(final Entity e)
	{
		/*
		 * 1. Has path info?
		 */
		final PathInfo pathInfo = eqs.get(e, PathInfo.class);
		if (pathInfo == null)
		{
			return true;
		}
		/*
		 * 2. Path has been computed correctly last time?
		 */
		if (pathInfo.getFindPathResult() == PathInfo.FindPathResult.NOT_FOUND)
		{
			return true;
		}
		/*
		 * 3. The target has been reached? TODO recompute or not?
		 */
		// if (pathInfo.isReached())return true;
		/*
		 * 4. Target cell location has changed?
		 */
		final EntityId targetEntityId = eqs.get(e, FollowingEntity.class)
				.getTarget();
		if (targetEntityId == null)
		{
			return true;
		}
		CellLocation targetCellLocation = eqs.get(targetEntityId, CellLocation.class);
		if (targetCellLocation == null)
		{
			// will be removed in the next iteration
			return false;
		}
		final int targetCell = targetCellLocation.getCellId();
		if (targetCell < 0)
		{
			return true;
		}
		if (targetCell != pathInfo.getEnd())
		{
			return true;
		}
		/*
		 * 5. Is in path?
		 */
		final ExtentsComponent exc = eqs.get(e, ExtentsComponent.class);
		final float radius = Math.max(exc.getX(), exc.getZ());
		final Vector3f position = eqs.get(e, Position.class).get();

		final boolean isInPath = nms.isInPath(pathInfo.getGeneratedPath(),
				position,
				radius);

		if (!isInPath)
		{
			return true;
		}

		return false;

	}

	@Override
	public void update()
	{
		this.entities.applyChanges();
		this.entities.forEach(this::recomputePathIfNeeded);
		// this.entities.getAddedEntities().forEach(this::recomputePathIfNeeded);
		// this.entities.getChangedEntities().forEach(this::recomputePathIfNeeded);
		this.entities.forEach(e -> updatePathInfo(e, tpf()));
	}

	private void updatePathInfo(final Entity e, final float tpf)
	{

		final PathInfo pI = eqs.get(e, PathInfo.class);
		final Position currentPosition = eqs.get(e, Position.class);
		final ExtentsComponent exc = eqs.get(e, ExtentsComponent.class);
		final float radius = Math.max(exc.getX(), exc.getZ());

		if (pI.getFindPathResult() == PathInfo.FindPathResult.NOT_FOUND
				|| pI.isReached())
		{
			eds.remove(e, PathInfo.class);
			eds.setComponent(e, new TargetReached());
			return;

		} else if (pI.getEnd() == eqs.get(e, CellLocation.class).getCellId())
		{
			// FIXME what to do here?
			// eds.remove(e, PathInfo.class);
		} else
		{
			// Vector3f target = new Vector3f();
			// target.set(this.setNextIndex(e));
			// eds.setComponent(e, new DesiredDirection(
			// target.getX() - currentLocation.x,
			// target.getZ() - currentLocation.z));
			final List<Vector3f> midPointsPath = pI.getMidPointPath();
			final int nseg = nearestLineSegment(currentPosition.get(),
					radius,
					midPointsPath);
			// check the cell where i am
			pI.setCellIndex(nseg);

			eds.setComponent(e, pI);
		}
		//
		if (hasReached(e, tpf))
		{
			pI.setReached(true);
			// eds.setComponent(e, pI);
			eds.remove(e, PathInfo.class);
			eds.setComponent(e, new TargetReached());
		}
	}

	private void recomputePathIfNeeded(final Entity e)
	{

		if (!pathMustBeRecomputed(e))
		{
			return;
		}

		final FollowingEntity followingEntity = eqs.get(e,
				FollowingEntity.class);
		final CellLocation entityCellLoc = eqs.get(e, CellLocation.class);
		final EntityId targetEntityId = followingEntity.getTarget();

		// Quizas no se ha puesto todavia
		final int targetCellId = getCellIdFromEntityId(targetEntityId);

		final List<Integer> cellPath = new ArrayList<Integer>();// new
																// IntArrayList();
		if (!entityCellLoc.isValid() || entityCellLoc.getCellId() < 0 || targetCellId < 0)
		{
			eds.setComponent(e, new PathInfo().withFindPathResult(FindPathResult.NOT_FOUND));
			return;
		}
		final FindPathResult pr = findCellPath(
				entityCellLoc.getCellId(),
				targetCellId,
				cellPath);

		PathInfo pI = null;
		if (pr != FindPathResult.NOT_FOUND)
		{
			// TODO optimize memory
			final TriangleNavigationMeshProcessor nms2 = (TriangleNavigationMeshProcessor) nms;
			final List<Vector3f> midPointPath = NavigationMeshUtil
					.toMidPointPath(cellPath, new ArrayList<>(), nms2::getCell);
			// midPointPath.forEach(v -> {
			// v.x += FastMath.nextRandomFloat();
			// v.z += FastMath.nextRandomFloat();
			//
			// });

			// List<Vector3f> midPointPath = cellPath
			// .stream()
			// .map(nms::getCellCenter)
			// .collect(Collectors.toList());
			final Position entityPosition = eqs.get(e, Position.class);
			final Position targetPosition = eqs.get(targetEntityId,
					Position.class);
			// add start & end
			midPointPath.add(0, entityPosition.get(new Vector3f()));
			midPointPath.add(targetPosition.get(new Vector3f()));

			pI = new PathInfo()
					.withFindPathResult(pr)
					.withStart(entityCellLoc.getCellId())
					.withEnd(targetCellId)
					.withGeneratedPath(cellPath)
					.withMidPointPath(midPointPath)
					.withCellIndex(0)
					.withReached(false);

			pI.setFunnel(((TriangleNavigationMeshProcessor) nms).funnel(
					entityPosition.get(new Vector3f()), pI.getGeneratedPath(),
					targetPosition.get(new Vector3f())));

		} else
		{
			pI = new PathInfo();
		}

		eds.setComponent(e, pI);
	}

	// private void drawDebug()
	// {
	//// if (ds==null) return;
	//// for (Entity e : entities)
	//// {
	//// PathInfo pI = eqs.get(e, PathInfo.class);
	//// if (pI != null)
	//// {
	//// List<Integer> genPath = pI.getGeneratedPath();
	//// for (int i = 0; i < genPath.size() - 1; i++)
	//// {
	//// ds.drawLine(BasicColor.Yellow,
	//// nms.getCellCenter(genPath.get(i)),
	//// nms.getCellCenter(genPath.get(i + 1)));
	//// }
	//// }
	//// }
	// }

	private int getCellIdFromEntityId(final EntityId eid)
	{
		final CellLocation cL = eqs.get(eid, CellLocation.class);
		if (cL != null)
			return cL.getCellId();
		return this.nms
				.findNearestCell(eqs.get(eid, Position.class).get());
	}

	public boolean hasReached(final Entity e, final float tpf)
	{
		final FollowingEntity followingEntity = eqs.get(e,
				FollowingEntity.class);
		final EntityId targetEntityId = followingEntity.getTarget();
		final Position entityTargetPosition = eqs.get(targetEntityId,
				Position.class);
		final ExtentsComponent exc = eqs.get(e, ExtentsComponent.class);
		final float radius = Math.max(exc.getX(), exc.getZ());
		return velocityDistance(e, entityTargetPosition.get(), tpf) <= radius;

	}

	private FindPathResult findCellPath(final int startCell, final int goalCell,
			final List<Integer> store)
	{
		switch (this.nms.findPath(startCell, goalCell, Integer.MAX_VALUE,
				store))
		{
		case COMPLETE_PATH_FOUND:
			return FindPathResult.COMPLETE_PATH_FOUND;
		case NOT_FOUND:
			return FindPathResult.NOT_FOUND;
		case PARTIAL_PATH_FOUND:
			return FindPathResult.PARTIAL_PATH_FOUND;
		default:
			throw new UnsupportedOperationException();

		}
	}

	public float velocityDistance(final Entity e, final Vector3f targetPos,
			final float tpf)
	{
		/*
		 * Entity Components
		 */
		final Position entityPosition = eqs.get(e, Position.class);
		final Speed speed = eqs.get(e, Speed.class);
		/*
		 * 
		 */

		final TempVars tmp = TempVars.get();
		//
		final Vector3f pos = entityPosition.get(tmp.vect1);
		final Vector3f vel = tmp.vect2
				.set(targetPos)
				.subtractLocal(pos)
				.normalizeLocal()
				.multLocal(speed.getValue());
		final Vector3f futurePos = pos.add(vel.mult(tpf, tmp.vect3), tmp.vect4);
		final float minDist = minimum_distance2D(pos, futurePos, targetPos);
		tmp.release();
		return minDist;
	}

	@Override
	public void simpleCleanup()
	{
		entities.release();
		entities.clear();
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
