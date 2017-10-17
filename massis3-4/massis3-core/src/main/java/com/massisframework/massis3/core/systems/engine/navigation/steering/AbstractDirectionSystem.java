package com.massisframework.massis3.core.systems.engine.navigation.steering;

import com.massisframework.massis3.commons.app.system.AbstractMassisSystem;
import com.massisframework.massis3.commons.pathfinding.navmesh.NavigationMeshProcessor;
import com.massisframework.massis3.core.systems.engine.navigation.DirectionSystem;
import com.massisframework.massis3.core.systems.engine.navigation.NavmeshHolderSystem;

public abstract class AbstractDirectionSystem extends AbstractMassisSystem
		implements DirectionSystem {

	protected NavigationMeshProcessor nms;

	@Override
	protected void simpleInitialize()
	{
		this.nms = getState(NavmeshHolderSystem.class).getTriNavMesh();
	}

}
