package com.massisframework.massis3.core.systems.engine.navigation.rvo2;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.massisframework.massis3.rvo.AgentNeighboursFinder;

import edu.unc.cs.gamma.rvo.RVO2Agent;
import edu.unc.cs.gamma.rvo.RVO2KdTree;
import edu.unc.cs.gamma.rvo.RVO2Obstacle;

public class KDTreeNeighboursFinder implements AgentNeighboursFinder {

	private final RVO2KdTree tree;
	private final Function<RVO2Agent, List<RVO2Obstacle>> accessor;

	public KDTreeNeighboursFinder(
			final Function<RVO2Agent, List<RVO2Obstacle>> componentProvider)
	{
		this.accessor = componentProvider;
		this.tree = new RVO2KdTree();
	}

	@Override
	public void computeAgentNeighbors(final RVO2Agent agent,
			final double rangeSq)
	{
		this.tree.computeAgentNeighbors(agent, rangeSq);
	}

	@Override
	public void computeObstacleNeighbors(final RVO2Agent rvo2Agent,
			final double rangeSq)
	{
		final List<RVO2Obstacle> obstacles = this.accessor.apply(rvo2Agent);
		for (final RVO2Obstacle obstacle : obstacles)
		{
			rvo2Agent.insertObstacleNeighbor(obstacle, rangeSq);
		}

	}

	@Override
	public void rebuild(final Collection<RVO2Agent> simAgents)
	{
		this.tree.rebuild(simAgents);
	}

}
