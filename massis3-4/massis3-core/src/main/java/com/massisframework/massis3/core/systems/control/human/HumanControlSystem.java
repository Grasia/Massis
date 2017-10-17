package com.massisframework.massis3.core.systems.control.human;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.jme3.math.Vector3f;
import com.massisframework.massis3.commons.app.system.AbstractMassisSystem;
import com.massisframework.massis3.commons.app.system.BackgroundTasksSystem;
import com.massisframework.massis3.commons.app.system.RequiresSystems;
import com.massisframework.massis3.core.assets.AssetReference;
import com.massisframework.massis3.core.assets.AssetReference.AssetType;
import com.massisframework.massis3.core.components.AnimationComponent;
import com.massisframework.massis3.core.components.ExtentsComponent;
import com.massisframework.massis3.core.components.Facing;
import com.massisframework.massis3.core.components.FollowingEntity;
import com.massisframework.massis3.core.components.Human;
import com.massisframework.massis3.core.components.Human.Age;
import com.massisframework.massis3.core.components.Human.Gender;
import com.massisframework.massis3.core.systems.required.EntityDataSystem;
import com.massisframework.massis3.core.components.Mass;
import com.massisframework.massis3.core.components.Model3DInfo;
import com.massisframework.massis3.core.components.PathInfo;
import com.massisframework.massis3.core.components.Position;
import com.massisframework.massis3.core.components.Scale;
import com.massisframework.massis3.core.components.Speed;
import com.massisframework.massis3.simulation.ecs.EntityComponentAccessor;
import com.massisframework.massis3.simulation.ecs.EntityComponentModifier;
import com.massisframework.massis3.simulation.ecs.GeneratesComponents;
import com.massisframework.massis3.simulation.ecs.RemovesComponents;
import com.massisframework.massis3.simulation.ecs.TracksComponents;
import com.simsilica.es.EntityId;

@RequiresSystems({ EntityDataSystem.class,
		BackgroundTasksSystem.class })
@GeneratesComponents({
		Position.class,
		Human.class,
		ExtentsComponent.class,
		Facing.class,
		Mass.class,
		Model3DInfo.class,
		Position.class,
		Scale.class,
		Speed.class,
		AnimationComponent.class, FollowingEntity.class
})
@TracksComponents({
		Position.class, PathInfo.class, FollowingEntity.class
})
@RemovesComponents({ FollowingEntity.class })
public class HumanControlSystem extends AbstractMassisSystem {

	private EntityComponentModifier eds;
	private Set<EntityId> humanIds;
	private EntityComponentAccessor eqs;

	@Override
	protected void simpleInitialize()
	{
		this.eds = getState(EntityDataSystem.class).createModifierFor(this);
		this.eqs = getState(EntityDataSystem.class).createAccessorFor(this);
		this.humanIds = ConcurrentHashMap.newKeySet();
	}

	@Override
	protected void onDisable()
	{

	}

	@Override
	protected void onEnable()
	{

	}

	@Override
	public void update()
	{

	}

	public Set<EntityId> getHumanIds()
	{
		return this.humanIds;
	}

	public CompletionStage<EntityId> createHuman(
			final Human.Gender gender,
			final Human.Age age,
			final Vector3f location)
	{
		try
		{
			final EntityId id = this.eds.createEntity();
			this.humanIds.add(id);
			eds.setComponents(id,
					new Position(location.x, location.y, location.z),
					new Facing(0, 0, 0, 1),
					new Human(gender, age),
					new Model3DInfo(bestModelFor(gender, age)),
					new Mass(80f),
					// HARDCODED
					new ExtentsComponent(0.35f, 0.779725f, 0.35f),
					// new Scale(0.47673997f, 0.779725f, 0.20348999f),
					new Scale(1, 1, 1),
					new Speed(4.5f));
			return CompletableFuture.completedFuture(id);
		} catch (Exception e)
		{
			return HumanCheckException.createAsStage(e);
		}

	}

	public CompletionStage<EntityId> animateHuman(long id, String animName)
	{
		try
		{
			if (!isHuman(id))
			{
				return HumanCheckException.createAsStage(id);
			}
			// FIXME check eagerly for existance
			eds.setComponent(id, new AnimationComponent(animName));
			return CompletableFuture.completedFuture(new EntityId(id));
		} catch (Exception e)
		{
			return HumanCheckException.createAsStage(e);
		}
	}

	public boolean isHuman(long id)
	{
		return isHuman(new EntityId(id));
	}

	public boolean isHuman(EntityId id)
	{
		return this.humanIds.contains(id);
	}

	public void checkHumanOrFail(long id)
	{
		if (!isHuman(id))
			throw HumanCheckException.create(id);
	}

	public void checkHumanOrFail(EntityId id)
	{
		if (!isHuman(id))
			throw HumanCheckException.create(id);
	}

	private AssetReference bestModelFor(final Gender gender, final Age age)
	{
		String model = null;
		switch (age)
		{
		case CHILD:
			model = "male-child1";
			break;
		case ELDER:
			model = "male-elder1";
			break;
		case NORMAL:
		case TEEN:
			if (gender == Gender.MALE)
			{
				model = "male-casual" + (Math.random() > 0.5 ? "1" : "2");
				break;
			} else
			{
				model = "female-casual" + (Math.random() > 0.5 ? "1" : "2");
				break;
			}

		default:
			throw new UnsupportedOperationException();

		}
		return new AssetReference(AssetType.ASSET, model + ".j3o");
	}

	public Vector3f getLocation(long id)
	{
		return getLocation(new EntityId(id));

	}

	public Vector3f getLocation(EntityId id)
	{
		if (!isHuman(id))
		{
			throw HumanCheckException.create(id);
		}
		Position pos = this.eqs.get(id, Position.class);
		if (pos == null)
		{
			throw new RuntimeException("The entity has no position");
		}
		return pos.get();

	}

	public CompletionStage<Boolean> isFollowingPath(long id)
	{
		if (!isHuman(id))
		{
			return HumanCheckException.createAsStage(id);
		}
		PathInfo pI = this.eqs.get(new EntityId(id), PathInfo.class);
		if (pI == null)
		{
			return CompletableFuture.completedFuture(false);
		} else
		{
			return CompletableFuture.completedFuture(true);
		}

	}

	public void destroyHuman(Long humanId)
	{
		if (this.isHuman(humanId))
		{
			//TODO set to update loop
			humanIds.remove(new EntityId(humanId));
			this.eds.removeEntity(new EntityId(humanId));
		}
	}

	private static <T> CompletableFuture<T> failedFuture(Exception ex)
	{
		CompletableFuture<T> cF = new CompletableFuture<>();
		cF.completeExceptionally(ex);
		return cF;
	}

	// TODO destroy moving entities, create a follow appState that controls
	// navmesh errors & such. TODO fail if no path found
	public CompletionStage<Boolean> moveTo(long humanId, Vector3f fromArray)
	{
		this.eds.remove(new EntityId(humanId), FollowingEntity.class);
		return this.getState(BackgroundTasksSystem.class).enqueueInUpdate(() -> {
			EntityId targetId = this.eds.createEntity();
			this.eds.setComponent(targetId, new Position(fromArray.x, fromArray.y, fromArray.z));
			FollowingEntity fE = new FollowingEntity(targetId);
			this.eds.setComponent(humanId, fE);
			return true;
		});
	}

	public CompletionStage<Boolean> stopMoving(long id)
	{
		if (!isHuman(id))
		{
			return HumanCheckException.createAsStage(id);
		}
		// TODO remove entity as well
		boolean result = this.eds.remove(new EntityId(id), FollowingEntity.class);
		return CompletableFuture.completedFuture(result);
	}

	public CompletionStage<List<Long>> getHumansInRange(long id, float range)
	{
		if (!isHuman(id))
		{
			return HumanCheckException.createAsStage(id);
		}
		EntityId currentId = new EntityId(id);
		// TODO performance, CPU & mem
		List<Long> ids = this.eqs.findEntities(null, Human.class, Position.class)
				.stream().filter(otherId -> !otherId.equals(currentId))
				.filter(otherId -> areIn2DRange(currentId, otherId, range))
				.map(EntityId::getId)
				.collect(Collectors.toList());
		return CompletableFuture.completedFuture(ids);

	}

	private boolean areIn2DRange(EntityId human1, EntityId human2, float range)
	{

		Position h1Pos = eqs.get(human1, Position.class);
		Position h2Pos = eqs.get(human2, Position.class);
		// TODO take into account height
		return h1Pos.distance2D(h2Pos) <= range;
	}

}
