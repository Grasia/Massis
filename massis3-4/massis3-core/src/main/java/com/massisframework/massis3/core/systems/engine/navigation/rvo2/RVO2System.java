package com.massisframework.massis3.core.systems.engine.navigation.rvo2;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.massisframework.massis3.commons.app.system.AbstractMassisSystem;
import com.massisframework.massis3.commons.app.system.RequiresSystems;
import com.massisframework.massis3.core.components.Position;
import com.massisframework.massis3.core.components.Speed;
import com.massisframework.massis3.core.systems.required.EntityDataSystem;
import com.massisframework.massis3.rvo.AgentNeighboursFinder;
import com.massisframework.massis3.simulation.ecs.EntityComponentAccessor;
import com.massisframework.massis3.simulation.ecs.EntityComponentModifier;
import com.massisframework.massis3.simulation.ecs.GeneratesComponents;
import com.massisframework.massis3.simulation.ecs.RemovesComponents;
import com.massisframework.massis3.simulation.ecs.TracksComponents;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;

import edu.unc.cs.gamma.rvo.RVO2Agent;

@TracksComponents({
		RVO2Component.class,
		Position.class,
		Speed.class,
})

@GeneratesComponents({
		RVO2DesiredDirection.class
})
@RemovesComponents({
		RVO2DesiredDirection.class
})
@RequiresSystems({
		EntityDataSystem.class,
})
public class RVO2System extends AbstractMassisSystem {

	private static final Logger log = LoggerFactory.getLogger(RVO2System.class);
	private EntitySet rvos;

	private Map<Integer, RVO2Agent> agents;
	private BiMap<EntityId, Integer> rvoAgentsMap;
	private AgentNeighboursFinder neighborsFinder;

	private EntityComponentAccessor eqs;
	private EntityComponentModifier eds;

	@Override
	public void simpleInitialize()
	{
		this.rvoAgentsMap = HashBiMap.create();
		this.eqs = getState(EntityDataSystem.class).createAccessorFor(this);
		this.eds = getState(EntityDataSystem.class).createModifierFor(this);
		this.rvos = eqs.getEntities(
				RVO2Component.class,
				Position.class,
				Speed.class);

		this.neighborsFinder = new KDTreeNeighboursFinder(a -> {
			// final EntityId eid = this.rvoAgentsMap.inverse().get(a.id);
			// TODO this.rvos.getEntity(id) instead
			// final RVO2Component rvo2 = eqs.get(eid, RVO2Component.class);
			// return rvo2.getNearbyObstacles();
			return Collections.emptyList();
		});
		this.rvoAgentsMap = HashBiMap.create();
		this.agents = new HashMap<>();
		this.rvos.applyChanges();
		this.insertAgents(rvos);
		this.updateAgents(rvos, 0);

	}

	@Override
	public void update()
	{
		if (rvos.applyChanges())
		{
			this.removeAgents(rvos.getRemovedEntities());
			this.insertAgents(rvos.getAddedEntities());
			this.updateAgents(rvos.getChangedEntities(), this.tpf());
		}
		this.publishChanges(this.tpf());
	}

	private void removeAgents(final Set<Entity> removedEntities)
	{
		for (final Entity e : removedEntities)
		{
			final Integer agentId = rvoAgentsMap.remove(e.getId());
			if (agentId != null)
			{
				this.agents.remove(agentId);
				eds.remove(e, RVO2DesiredDirection.class);

			} else
			{
				if (log.isErrorEnabled())
				{
					log.error("AgentId not found when removing");
				}
			}
		}
	}

	private void updateAgents(final Set<Entity> entities, final float tpf)
	{
		for (final Entity e : entities)
		{
			final RVO2Component rvo2C = eqs.get(e, RVO2Component.class);

			final Position pos = eqs.get(e, Position.class);
			// Velocity vel = eqs.get(e,Velocity.class);

			// Set positions
			final int agentId = rvoAgentsMap.get(e.getId());
			final RVO2Agent agent = this.agents.get(agentId);
			this.updateAgent(agent, e);
			final Vector2D position = new Vector2D(
					pos.getX(),
					pos.getZ());

			this.setAgentPosition(agentId, position);

			final Vector2D goal = new Vector2D(rvo2C.getGoalX(),
					rvo2C.getGoalZ());

			Vector2D goalVector = goal.subtract(position);
			{
				// normalize,if possible.
				final double s = goalVector.getNorm();
				if (s != 0)
				{
					goalVector = goalVector.scalarMultiply(1 / s);
					// mult by actual max speed
					goalVector = goalVector.scalarMultiply(agent.maxSpeed);
				}
				this.setAgentPreferredVelocity(agentId, goalVector);
			}
			agent.velocity = new Vector2D(agent.preferredVelocity.getX(),
					agent.preferredVelocity.getY());
			// Perturb a little to avoid deadlocks due to perfect symmetry.
			final double angle = ThreadLocalRandom.current().nextDouble() * 2.0
					* Math.PI;
			final double distance = ThreadLocalRandom.current().nextDouble()
					* 0.0001;
			this.setAgentPreferredVelocity(agentId,
					this.getAgentPreferredVelocity(agentId)
							.add(new Vector2D(Math.cos(angle), Math.sin(angle))
									.scalarMultiply(distance)));
		}

	}

	private void publishChanges(final float tpf)
	{
		neighborsFinder.rebuild(this.agents.values());
		for (final RVO2Agent agent : agents.values())
		{
			agent.computeNeighbors(neighborsFinder);
			removeNeighboursByHeight(agent);
			agent.computeNewVelocity(tpf);
		}
		agents.values().forEach(r -> r.updateVelocity(tpf));

		this.rvoAgentsMap.forEach((eId, agentId) -> {
			final Vector2D v = this.getAgentVelocity(agentId);
			if (!Float.isNaN((float) v.getX())
					&& !Float.isNaN((float) v.getY()))
				eds.setComponent(eId, new RVO2DesiredDirection((float) v.getX(),
						(float) v.getY()));
		});
	}

	private void removeNeighboursByHeight(final RVO2Agent agent)
	{
		final float agentHeight = 1.8f;
		final EntityId agentEntityId = this.rvoAgentsMap.inverse()
				.get(agent.id);
		final float agentElev = eqs.get(agentEntityId, Position.class).getY();
		agent.agentNeighbors.removeIf((p) -> {
			// a lo bestia
			final RVO2Agent neighRVO = p.getSecond();
			final EntityId neighEntityId = this.rvoAgentsMap.inverse()
					.get(neighRVO.id);
			/*
			 * TODO Check if was already removed from simulation, that causes
			 * null
			 */
			if (neighEntityId == null)
			{
				return true;
			}
			final Position neighPosition = eqs.get(neighEntityId,
					Position.class);
			final float neighElev = neighPosition.getY();
			return agentElev + agentHeight < neighElev;
		});

	}

	private void insertAgents(final Set<Entity> addedEntities)
	{
		for (final Entity e : addedEntities)
		{
			if (!this.rvoAgentsMap.containsKey(e.getId()))
			{
				final int agentId = this.addAgent(e);
				this.rvoAgentsMap.put(e.getId(), agentId);
			}
		}

	}

	// RVO2 Operations

	private static final AtomicInteger AGENT_ID_COUNT = new AtomicInteger();

	/**
	 * Adds a new agent with default properties to the simulation.
	 *
	 * @param position
	 *            The two-dimensional starting position of this agent.
	 * @return The number of the agent, or -1 when the agent defaults have not
	 *         been set.
	 */
	private int addAgent(final Entity e)
	{
		final RVO2Agent agent = new RVO2Agent();
		agent.id = AGENT_ID_COUNT.getAndIncrement();// agents.size();
		agents.put(agent.id, agent);
		this.updateAgent(agent, e);
		return agent.id;
	}

	private void updateAgent(final RVO2Agent agent, final Entity e)
	{
		final RVO2Component rvo2Component = eqs.get(e, RVO2Component.class);
		final Position position = eqs.get(e, Position.class);
		final Speed speed = eqs.get(e, Speed.class);
		final float radius = rvo2Component.getRadius();
		agent.maxNeighbors = rvo2Component.getMaxNeighbors();
		agent.maxSpeed = speed.getValue();
		agent.neighborDistance = rvo2Component.getNeighborDistance();
		agent.position = new Vector2D(position.getX(), position.getZ());
		agent.radius = radius;
		agent.timeHorizonAgents = rvo2Component.getTimeHorizonAgents();
		agent.timeHorizonObstacles = rvo2Component.getTimeHorizonObstacles();
		// agent.velocity = new
		// Vector2D(rvo2Component.getVelocity().x,rvo2Component.getVelocity().y);
	}

	/**
	 * Returns the time horizon of a specified agent.
	 *
	 * @param agentNo
	 *            The number of the agent whose time horizon is to be retrieved.
	 * @return The present time horizon of the agent.
	 */
	public double getAgentTimeHorizonAgents(final int agentNo)
	{
		return agents.get(agentNo).timeHorizonAgents;
	}

	/**
	 * Returns the two-dimensional linear velocity of a specified agent.
	 *
	 * @param agentNo
	 *            The number of the agent whose two-dimensional linear velocity
	 *            is to be retrieved.
	 * @return The present two-dimensional linear velocity of the agent.
	 */
	private Vector2D getAgentVelocity(final int agentNo)
	{
		return agents.get(agentNo).velocity;
	}

	/**
	 * Sets the two-dimensional position of a specified agent.
	 *
	 * @param agentNo
	 *            The number of the agent whose two-dimensional position is to
	 *            be modified.
	 * @param position
	 *            The replacement of the two-dimensional position.
	 */
	private void setAgentPosition(final int agentNo, final Vector2D position)
	{
		agents.get(agentNo).position = position;
	}

	/**
	 * Sets the two-dimensional preferred velocity of a specified agent.
	 *
	 * @param agentNo
	 *            The number of the agent whose two-dimensional preferred
	 *            velocity is to be modified.
	 * @param preferredVelocity
	 *            The replacement of the two-dimensional preferred velocity.
	 */
	private void setAgentPreferredVelocity(final int agentNo,
			final Vector2D preferredVelocity)
	{
		agents.get(agentNo).preferredVelocity = preferredVelocity;
		// agents.get(agentNo).preferredVelocity = preferredVelocity.normalize()
		// .scalarMultiply(agents.get(agentNo).maxSpeed);
	}

	private Vector2D getAgentPreferredVelocity(final int agentNo)
	{
		return agents.get(agentNo).preferredVelocity;
	}

	@Override
	public void simpleCleanup()
	{
		this.rvos.release();
		this.rvos.clear();
		this.agents.clear();
		this.agents = null;
		this.rvoAgentsMap.clear();
		this.rvoAgentsMap = null;
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
