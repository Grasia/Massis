package com.massisframework.massis3.core.systems.debug;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.massisframework.massis3.commons.app.system.AbstractMassisSystem;
import com.massisframework.massis3.commons.app.system.BackgroundTasksSystem;
import com.massisframework.massis3.commons.app.system.RequiresSystems;
import com.massisframework.massis3.commons.spatials.Materials;
import com.massisframework.massis3.core.systems.engine.navigation.UniformGridSystem;

@RequiresSystems({
		UniformGridSystem.class,
		BackgroundTasksSystem.class
})
public class ShowUniformGridGraphAppSystem extends AbstractMassisSystem implements DebugSystem {

	private UniformGridSystem ug;
	private BackgroundTasksSystem taskExecutor;
	private static final Logger log = LoggerFactory.getLogger(ShowUniformGridGraphAppSystem.class);
	private AtomicBoolean loadMeshCalled;
	private Node meshNode;

	@Override
	protected void simpleInitialize()
	{
		this.loadMeshCalled = new AtomicBoolean(false);
		this.ug = getState(UniformGridSystem.class);
		this.taskExecutor = getState(BackgroundTasksSystem.class);
		this.meshNode = new Node();
	}

	@Override
	protected void onDisable()
	{

		if (log.isInfoEnabled())
		{
			log.info("Disabling showUniformGG");
		}
		this.graphicalEnqueue(systemNode -> {
			this.meshNode.removeFromParent();
		});
	}

	@Override
	protected void onEnable()
	{
		createMeshGeom();
		this.graphicalEnqueue(systemNode -> {
			systemNode.attachChild(meshNode);
		});
	}

	private void createMeshGeom()
	{
		if (loadMeshCalled.getAndSet(true))
		{
			return;
		}
		taskExecutor
				.enqueueInExecutor(this.ug::getUniformGridMesh)
				.thenApply(mesh -> {
					Geometry connGeom = new Geometry("cmesh", mesh);
					Material heatMaterial = Materials.newUnshaded(ColorRGBA.Blue);
					connGeom.setMaterial(heatMaterial);
					return connGeom;
				}).thenAccept(geometry -> {
					this.taskExecutor.enqueueInExecutor(() -> {
						this.meshNode.attachChild(geometry);
					});
				});
	}

	@Override
	public void update()
	{

	}

	@Override
	public void graphicalUpdate(Node systemNode)
	{
		
	}

}
