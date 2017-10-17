package com.massisframework.massis3.rvo;

import java.util.Collection;

import edu.unc.cs.gamma.rvo.RVO2Agent;

public interface AgentNeighboursFinder {

	
	public void computeAgentNeighbors(RVO2Agent agent, double rangeSq);

	public void computeObstacleNeighbors(RVO2Agent rvo2Agent, double rangeSq);

	void rebuild(Collection<RVO2Agent> simAgents);
}
