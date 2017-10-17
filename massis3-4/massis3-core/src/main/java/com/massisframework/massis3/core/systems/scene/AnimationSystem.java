package com.massisframework.massis3.core.systems.scene;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.Animation;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.massisframework.massis3.commons.app.control.AnimationAutoEnableControl;
import com.massisframework.massis3.commons.app.system.AbstractMassisSystem;
import com.massisframework.massis3.commons.app.system.RequiresSystems;
import com.massisframework.massis3.commons.spatials.Spatials;
import com.massisframework.massis3.core.components.AnimationComponent;
import com.massisframework.massis3.core.components.Model3DInfo;
import com.massisframework.massis3.core.systems.required.EntityDataSystem;
import com.massisframework.massis3.core.systems.required.SceneLoaderSystem;
import com.massisframework.massis3.simulation.ecs.EntityComponentAccessor;
import com.massisframework.massis3.simulation.ecs.TracksComponents;
import com.simsilica.es.Entity;
import com.simsilica.es.EntitySet;

@RequiresSystems({
		EntityDataSystem.class,
		SceneGraphSystem.class,
		SceneLoaderSystem.class
})
@TracksComponents({
		AnimationComponent.class,
		Model3DInfo.class
})
/**
 * Tracks entities with animation components, and performs all the necessary
 * operations for applying them in the scene graph
 * 
 * @author rpax
 *
 */
public class AnimationSystem extends AbstractMassisSystem {

	private EntitySet entities;
	private EntityComponentAccessor eqs;

	@Override
	public void simpleInitialize()
	{
		this.eqs = this.getState(EntityDataSystem.class).createAccessorFor(this);
	}

	@Override
	protected void onEnable()
	{
		this.entities = this.eqs.getEntities(
				AnimationComponent.class,
				Model3DInfo.class);
		this.entities.applyChanges();
		this.entities.forEach(this::updateAnim);
	}

	@Override
	protected void onDisable()
	{
		this.entities.forEach(this::removeAnim);
		this.entities.release();
		this.entities.clear();
	}

	private void removeAnim(final Entity e)
	{
		final Spatial sp = getState(SceneGraphSystem.class).getSpatial(e.getId());
		if (sp != null)
		{
			final AnimControl animControl = Spatials.getFirstControl(sp,
					AnimControl.class);
			if (animControl != null)
			{
				animControl.clearChannels();
			}
		}

	}

	private void updateAnim(final Entity e)
	{

		final String animName = e.get(AnimationComponent.class).getAnimationName();
		final Spatial sp = getState(SceneGraphSystem.class).getSpatial(e.getId());
		final AnimControl animControl = Spatials.getFirstControl(sp,
				AnimControl.class);
		sp.addControl(new AnimationAutoEnableControl(3, 100f));
		if (!animControl.getAnimationNames().contains(animName))
		{
			animControl.clearChannels();
			final Animation anim = this.getState(SceneLoaderSystem.class).loadAnimation(animName);
			animControl.addAnim(anim);
		}
		final int numChannels = animControl.getNumChannels();

		AnimChannel channel = null;
		if (numChannels == 0)
		{
			channel = animControl.createChannel();
		} else
		{
			channel = animControl.getChannel(0);
		}

		channel.setAnim(animName, 5);
	}

	@Override
	public void update()
	{
		
	}

	@Override
	public void graphicalUpdate(Node systemNode)
	{
		if (this.entities.applyChanges())
		{
			this.entities.getAddedEntities().forEach(this::updateAnim);
			this.entities.getChangedEntities().forEach(this::updateAnim);
			this.entities.getRemovedEntities().forEach(this::removeAnim);
		}
	}
}
