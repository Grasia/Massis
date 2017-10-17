package com.massisframework.massis3.core.systems.engine.navigation;

import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.massisframework.massis3.commons.app.system.AbstractMassisSystem;
import com.massisframework.massis3.commons.app.system.RequiresSystems;
import com.massisframework.massis3.commons.pathfinding.UniformGridGraph;
import com.massisframework.massis3.commons.pathfinding.UniformGridPathFinder;
import com.massisframework.massis3.core.systems.required.SceneLoaderSystem;

@RequiresSystems({
		SceneLoaderSystem.class
})
public class UniformGridSystem extends AbstractMassisSystem {

	private UniformGridGraph uniformGridGraph;
	private UniformGridPathFinder pathFinder;
	private Mesh gridMesh;

	@Override
	protected void simpleInitialize()
	{
		this.uniformGridGraph = getState(SceneLoaderSystem.class).loadUniformGridGraph();
	}

	public Mesh getUniformGridMesh()
	{
		synchronized (this)
		{
			if (this.gridMesh == null)
			{
				this.gridMesh = loadUniformGridMesh();
			}
		}
		return this.gridMesh;
	}

	private Mesh loadUniformGridMesh()
	{
		return this.uniformGridGraph.loadUniformGridMesh();
	}

	public void setWeight(int nodeId, float weight)
	{
		this.getPathFinder().setWeight(nodeId, weight);
	}

	public float getWeight(int nodeId)
	{
		return this.getPathFinder().getWeight(nodeId);
	}

	public UniformGridPathFinder getPathFinder()
	{
		if (this.pathFinder == null)
		{
			this.pathFinder = new UniformGridPathFinder(uniformGridGraph);
		}
		return this.pathFinder;
	}

	public int getNumEdges()
	{
		return uniformGridGraph.getNumEdges();
	}

	public float getNodePositionX(int nodeId)
	{
		return uniformGridGraph.getNodePositionX(nodeId);
	}

	public float getNodePositionY(int nodeId)
	{
		return uniformGridGraph.getNodePositionY(nodeId);
	}

	public float getNodePositionZ(int nodeId)
	{
		return uniformGridGraph.getNodePositionZ(nodeId);
	}

	public int getNumLinks(int nodeId)
	{
		return uniformGridGraph.getNumLinks(nodeId);
	}

	public int getNumNodes()
	{
		return uniformGridGraph.getNumNodes();
	}

	public Vector3f getRandomNodePosition()
	{
		return uniformGridGraph.getRandomNodePosition();
	}

	public Iterable<Integer> getLinkIds(int nodeId)
	{
		return uniformGridGraph.getLinkIds(nodeId);
	}

	@Deprecated
	public UniformGridGraph getGraph()
	{
		return this.uniformGridGraph;
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

}
