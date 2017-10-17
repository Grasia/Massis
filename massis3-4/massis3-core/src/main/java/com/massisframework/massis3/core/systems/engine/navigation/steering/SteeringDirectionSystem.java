package com.massisframework.massis3.core.systems.engine.navigation.steering;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.massisframework.massis3.commons.app.system.AbstractMassisSystem;
import com.massisframework.massis3.commons.app.system.RequiresSystems;
import com.massisframework.massis3.commons.collections.EntityIdObjectMap;
import com.massisframework.massis3.commons.pathfinding.navmesh.NavigationMeshProcessor;
import com.massisframework.massis3.commons.steering.SteeringUtil;
import com.massisframework.massis3.core.components.BoidComponent;
import com.massisframework.massis3.core.components.CollisionFreeVelocity;
import com.massisframework.massis3.core.components.DesiredDirection;
import com.massisframework.massis3.core.components.ExtentsComponent;
import com.massisframework.massis3.core.components.Path2DComponent;
import com.massisframework.massis3.core.components.PathInfo;
import com.massisframework.massis3.core.components.Position;
import com.massisframework.massis3.core.components.Speed;
import com.massisframework.massis3.core.systems.engine.navigation.CellLocation;
import com.massisframework.massis3.core.systems.engine.navigation.DirectionSystem;
import com.massisframework.massis3.core.systems.engine.navigation.NavmeshHolderSystem;
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
		Speed.class,
		PathInfo.class,
		ExtentsComponent.class,
		CellLocation.class,
		CollisionFreeVelocity.class,
		// ---------------------
		// UniformGridPath.class,
		// ---------------------
})
@GeneratesComponents({

		Path2DComponent.class,
		BoidComponent.class,
		DesiredDirection.class
})
@RemovesComponents({

		Path2DComponent.class,
		BoidComponent.class,
		DesiredDirection.class
})
@RequiresSystems({
		EntityDataSystem.class,
		NavmeshHolderSystem.class
})
public class SteeringDirectionSystem extends AbstractDirectionSystem {

	private EntityComponentAccessor eqs;
	private EntityComponentModifier eds;
	private EntitySet entities;
	private EntityIdObjectMap<Path2DComponent> paths2d;
	private EntityIdObjectMap<BoidComponent> boids;
	private static float DEFAULT_MAX_FORCE = 10000;

	private static final Logger log = LoggerFactory.getLogger(SteeringDirectionSystem.class);

	@Override
	public void simpleInitialize()
	{
		super.simpleInitialize();
		this.eqs = getState(EntityDataSystem.class).createAccessorFor(this);
		this.eds = getState(EntityDataSystem.class).createModifierFor(this);

		this.paths2d = new EntityIdObjectMap<>();
		this.boids = new EntityIdObjectMap<>();

		// TODO move to enable
		this.entities = this.eqs.getEntities(
				Position.class,
				Speed.class,
				PathInfo.class,
				ExtentsComponent.class,
				CellLocation.class,
				// ---------------------
				// UniformGridPath.class,
				// ---------------------
				ExtentsComponent.class);
		this.entities.forEach(this::addEntity);
	}

	private void addEntity(final Entity e)
	{
		final float speed = eqs.get(e, Speed.class).getValue();
		final Vector3f location = eqs.get(e, Position.class).get();
		final ExtentsComponent exc = eqs.get(e, ExtentsComponent.class);
		final float radius = Math.max(exc.getX(), exc.getZ());
		final PathInfo pI = eqs.get(e, PathInfo.class);
		final Vector2f position2d = new Vector2f(location.x, location.z);
		if (pI.getMidPointPath().size() == 0)
			return;
		// final Vector3f firstPoint = pI.getMidPointPath().get(1);
		// final Vector2f target2d = new Vector2f(firstPoint.x, firstPoint.z);

		final float maxforce = DEFAULT_MAX_FORCE;

		// Path 2d
		final Path2DComponent path2d = new Path2DComponent();
		// TODO optimize
		pI.getFunnel()
				.stream()
				.map(v3 -> toVector2DWithNoise(v3))
				.forEach(v2 -> path2d.getPath().add(v2));
		if (path2d.getPath().isEmpty())
		{

			if (log.isInfoEnabled())
			{
				log.info("path2d is empty");
			}

			// eqs.get(e, UniformGridPath.class).getPath()
			// .stream().map(v3 -> toVector2DWithNoise(v3))
			// .forEach(v2 -> path2d.getPath().add(v2));
			return;
		}

		final Vector2f velocity = path2d.getPath().get(0).clone()
				.subtractLocal(position2d)
				.normalizeLocal()
				.mult(speed);

		final BoidComponent boid = new BoidComponent()
				.withMaxforce(maxforce)
				.withMaxspeed(speed)
				.withPosition(position2d)
				.withRadius(radius)
				.withVelocity(velocity);

		this.boids.put(e.getId(), boid);
		this.eds.setComponent(e, boid);

		this.paths2d.put(e.getId(), path2d);
		this.eds.setComponent(e, path2d);

	}

	private Vector2f toVector2DWithNoise(Vector3f v3)
	{
		return new Vector2f(
				v3.x + FastMath.nextRandomFloat() * 1f * FastMath.nextRandomInt(-1, 1),
				v3.z + FastMath.nextRandomFloat() * 1f * FastMath.nextRandomInt(-1, 1));
	}

	private void updateEntity(final Entity e)
	{

		// final BoidComponent boid = eqs.get(e, BoidComponent.class);
		final BoidComponent boid = this.boids.get(e.getId());
		if (boid == null)
			return;
		final Speed speed = eqs.get(e, Speed.class);
		// final Path2DComponent path2dC = eqs.get(e, Path2DComponent.class);
		final Path2DComponent path2dC = this.paths2d.get(e.getId());
		final Position position = eqs.get(e, Position.class);
		//
		final Vector3f location = position.get(new Vector3f());

		boid.setPosition(new Vector2f(location.x, location.z));
		final ExtentsComponent exc = eqs.get(e, ExtentsComponent.class);
		float radius = Math.max(exc.getX(), exc.getZ());

		final CollisionFreeVelocity cfv = eqs.get(e, CollisionFreeVelocity.class);
		final CellLocation cL = eqs.get(e, CellLocation.class);

		if (cfv != null)
		{
			boid.setVelocity(new Vector2f(cfv.getX(), cfv.getZ()));
		} else
		{
			boid.setVelocity(new Vector2f(0.001f, 0.001f));
		}
		float framesAhead = 10;
		if (cL != null)
		{
			final Vector3f nearestObstaclePoint = nms
					.nearestObstaclePoint(location,
							cL.getCellId());

			final float distToNearest = location.distance(nearestObstaclePoint);
			radius = distToNearest / 2;
			framesAhead = (float) Math.ceil(distToNearest)
					* (0.5f + FastMath.nextRandomFloat() / 2);
		}

		final float maxforce = DEFAULT_MAX_FORCE;

		final Vector2f followForce = SteeringUtil
				.follow(
						boid.getPosition(),
						boid.getVelocity(),
						framesAhead,
						radius,
						speed.getValue(),
						maxforce,
						path2dC.getPath());

		this.boids.put(e.getId(), boid);
		this.eds.setComponent(e, boid);

		this.paths2d.put(e.getId(), path2dC);
		this.eds.setComponent(e, path2dC);

		eds.setComponent(e, new DesiredDirection(followForce.x, 0, followForce.y));
	}

	private void removeEntity(final Entity e)
	{
		this.boids.remove(e.getId());
		this.eds.remove(e, BoidComponent.class);
		this.eds.remove(e, Path2DComponent.class);
		this.paths2d.remove(e.getId());
		this.eds.remove(e, DesiredDirection.class);
	}

	@Override
	public void update()
	{
		if (this.entities.applyChanges())
		{
			this.entities.getRemovedEntities().forEach(this::removeEntity);
			this.entities.getAddedEntities().forEach(this::addEntity);
			this.entities.getChangedEntities().forEach(this::updateEntity);
		}
		// this.entities.forEach(this::updateEntity);
	}

	@Override
	protected void onDisable()
	{
		// TODO Auto-generated method stub
	}

	@Override
	protected void onEnable()
	{
		// TODO Auto-generated method stub
	}

}
