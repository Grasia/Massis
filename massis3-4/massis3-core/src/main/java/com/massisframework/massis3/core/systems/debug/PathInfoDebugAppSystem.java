package com.massisframework.massis3.core.systems.debug;

import java.util.List;
import java.util.stream.Collectors;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.massisframework.massis3.commons.app.system.AbstractMassisSystem;
import com.massisframework.massis3.commons.app.system.DebugShapesSystem;
import com.massisframework.massis3.commons.app.system.RequiresSystems;
import com.massisframework.massis3.commons.app.system.debug.BasicColor;
import com.massisframework.massis3.core.components.Path2DComponent;
import com.massisframework.massis3.core.components.PathInfo;
import com.massisframework.massis3.core.components.Position;
import com.massisframework.massis3.core.systems.engine.navigation.NavmeshHolderSystem;
import com.massisframework.massis3.core.systems.required.EntityDataSystem;
import com.massisframework.massis3.simulation.ecs.EntityComponentAccessor;
import com.massisframework.massis3.simulation.ecs.TracksComponents;
import com.simsilica.es.Entity;
import com.simsilica.es.EntitySet;

@RequiresSystems({
		EntityDataSystem.class,
		NavmeshHolderSystem.class,
		DebugShapesSystem.class
})
@TracksComponents({
		PathInfo.class,
		Position.class
})
public class PathInfoDebugAppSystem extends AbstractMassisSystem implements DebugSystem {

	private EntitySet pathEntities;
	private NavmeshHolderSystem nms;
	private EntityComponentAccessor eqs;

	@Override
	public void simpleInitialize()
	{
		this.nms = this.getState(NavmeshHolderSystem.class);
	}

	@Override
	protected void onEnable()
	{
		this.eqs = this.getState(EntityDataSystem.class).createAccessorFor(this);
		this.pathEntities = this.eqs.getEntities(
				PathInfo.class,
				Position.class
		// Path2DComponent.class
		);
	}

	@Override
	protected void onDisable()
	{
		this.pathEntities.release();
		this.pathEntities.clear();
	}

	@Override
	public void update()
	{
	}

	@Override
	public void graphicalUpdate(Node systemNode)
	{

		this.pathEntities.applyChanges();
		for (Entity e : this.pathEntities)
		{
			Position position = e.get(Position.class);
			PathInfo pI = e.get(PathInfo.class);
			if (pI != null)
			{

				DebugShapesSystem ds = getState(DebugShapesSystem.class);
				List<Integer> genPath = pI.getGeneratedPath();
				for (int i = 0; i < genPath.size() - 1; i++)
				{
					ds.drawLine(BasicColor.Green,
							nms.getCellCenter(genPath.get(i)),
							nms.getCellCenter(genPath.get(i + 1)));
				}
				List<Vector3f> midPointPath = pI.getMidPointPath();
				for (int i = 0; i < midPointPath.size() - 1; i++)
				{
					ds.drawLine(BasicColor.Blue,
							midPointPath.get(i),
							midPointPath.get(i + 1));
				}
				List<Vector3f> funnel = pI.getFunnel();
				if (funnel != null && funnel.size() > 0)
				{
					for (int i = 0; i < funnel.size() - 1; i++)
					{
						ds.drawLine(BasicColor.Cyan,
								funnel.get(i),
								funnel.get(i + 1));
					}
				}
				if (e.get(Path2DComponent.class) != null)
				{
					List<Vector3f> pathSteering = e.get(Path2DComponent.class)
							.getPath()
							.stream()
							.map(v2 -> new Vector3f(v2.x, position.getY(), v2.y))
							.collect(Collectors.toList());
					for (int i = 0; i < pathSteering.size() - 1; i++)
					{
						ds.drawLine(BasicColor.Orange,
								pathSteering.get(i),
								pathSteering.get(i + 1));
					}
				}

			}
		}

	}

}
