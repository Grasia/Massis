/**
 * 
 */
package com.massisframework.massis3.core.systems.debug;

import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.massisframework.massis3.commons.app.system.AbstractMassisSystem;
import com.massisframework.massis3.commons.app.system.RequiresSystems;
import com.massisframework.massis3.commons.spatials.Materials;
import com.massisframework.massis3.core.systems.engine.navigation.NavmeshHolderSystem;

/**
 * @author rpax
 *
 */
@RequiresSystems({
		NavmeshHolderSystem.class
})
public class NavmeshVisualizerSystem extends AbstractMassisSystem implements DebugSystem {

	private Geometry navmeshGeom;
	private NavmeshHolderSystem navmeshHolder;
	private Mesh rawmesh;

	@Override
	protected void simpleInitialize()
	{
		this.navmeshHolder = getState(NavmeshHolderSystem.class);
		this.rawmesh = this.navmeshHolder.getRawmesh();

		this.navmeshGeom = new Geometry("NavmeshGeom", rawmesh);
		this.navmeshGeom.setMaterial(Materials.newUnshaded(ColorRGBA.Red, true));
	}

	@Override
	protected void onDisable()
	{
		this.graphicalEnqueue(node -> {
			this.navmeshGeom.removeFromParent();
		});
	}

	@Override
	protected void onEnable()
	{
		this.graphicalEnqueue(node -> {
			node.attachChild(this.navmeshGeom);
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
