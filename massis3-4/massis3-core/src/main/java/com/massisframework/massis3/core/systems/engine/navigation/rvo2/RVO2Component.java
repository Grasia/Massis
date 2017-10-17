package com.massisframework.massis3.core.systems.engine.navigation.rvo2;

import com.simsilica.es.EntityComponent;

/**
 * XXX FIXME adapt to RVO2 system correctly
 * 
 * @author rpax
 *
 */
public class RVO2Component implements EntityComponent {

	private double neighborDistance;
	private int maxNeighbors;
	private double timeHorizonAgents;
	private double timeHorizonObstacles;
	private float goalX, goalZ;
	private float radius;

	public RVO2Component()
	{
		// this(1);
	}

	public RVO2Component(final double maxSpeed)
	{
		// this(20.0, 20, /* timeHorizonAgents */10.0, 20.0);
	}

	public double getNeighborDistance()
	{
		return neighborDistance;
	}

	public RVO2Component setNeighborDistance(final double neighborDistance)
	{
		this.neighborDistance = neighborDistance;
		return this;
	}

	public int getMaxNeighbors()
	{
		return maxNeighbors;
	}

	public RVO2Component setMaxNeighbors(final int maxNeighbors)
	{
		this.maxNeighbors = maxNeighbors;
		return this;
	}

	public double getTimeHorizonAgents()
	{
		return timeHorizonAgents;
	}

	public RVO2Component setTimeHorizonAgents(final double timeHorizonAgents)
	{
		this.timeHorizonAgents = timeHorizonAgents;
		return this;
	}

	public double getTimeHorizonObstacles()
	{
		return timeHorizonObstacles;
	}

	public RVO2Component setTimeHorizonObstacles(
			final double timeHorizonObstacles)
	{
		this.timeHorizonObstacles = timeHorizonObstacles;
		return this;
	}

	public float getGoalX()
	{
		return goalX;
	}

	public void setGoalX(final float goalX)
	{
		this.goalX = goalX;
	}

	public float getGoalZ()
	{
		return goalZ;
	}

	public void setGoalZ(final float goalZ)
	{
		this.goalZ = goalZ;
	}

	public void setRadius(final float r)
	{
		this.radius = r;
	}

	public float getRadius()
	{
		return radius;
	}

	// private RVO2Component(
	// double neighborDistance,
	// int maxNeighbors,
	// double timeHorizonAgents,
	// double timeHorizonObstacles)
	// {
	//
	// this.neighborDistance = neighborDistance;
	// this.maxNeighbors = maxNeighbors;
	// this.timeHorizonAgents = timeHorizonAgents;
	// this.timeHorizonObstacles = timeHorizonObstacles;
	// this.nearbyObstacles = new ArrayList<>();
	// }

}
