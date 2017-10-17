/*
 * $Id$
 *
 * Copyright (c) 2013 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.massisframework.massis3.core.systems.scene;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.util.TempVars;
import com.massisframework.massis3.commons.app.control.InterpolationControl;
import com.massisframework.massis3.commons.app.system.AbstractMassisSystem;
import com.massisframework.massis3.commons.app.system.RequiresSystems;
import com.massisframework.massis3.core.components.Facing;
import com.massisframework.massis3.core.components.Model3DInfo;
import com.massisframework.massis3.core.components.Position;
import com.massisframework.massis3.core.components.Scale;
import com.massisframework.massis3.core.systems.required.EntityDataSystem;
import com.massisframework.massis3.simulation.ecs.EntityComponentAccessor;
import com.massisframework.massis3.simulation.ecs.TracksComponents;
import com.simsilica.es.Entity;
import com.simsilica.es.EntitySet;

/**
 * Based on Paul Speed's examples
 *
 * @author rpax
 * @author Paul Speed
 */
@TracksComponents({
		Position.class,
		Scale.class,
		Facing.class,
		Model3DInfo.class
})
@RequiresSystems({
		EntityDataSystem.class,
		SceneGraphSystem.class
})
public class ModelPositionSystem extends AbstractMassisSystem {

	private EntitySet entities;
	private EntityComponentAccessor eqs;
	private static final Logger log = LoggerFactory.getLogger(ModelPositionSystem.class);
	boolean firstTick = true;

	@Override
	public void simpleInitialize()
	{
		this.eqs = this.getState(EntityDataSystem.class).createAccessorFor(this);
	}

	@Override
	protected void onDisable()
	{
		this.entities.release();
		this.entities.clear();
	}

	@Override
	protected void onEnable()
	{
		this.entities = this.eqs
				.getEntities(
						Position.class,
						Scale.class,
						Facing.class,
						Model3DInfo.class);

		this.entities.applyChanges();
		updateModels(this.entities, tpf());
	}

	@Override
	public void update()
	{
		refreshModels(tpf());
	}

	protected void updateModelSpatial(final Entity e, final Spatial s, float tpf)
	{
		this.graphicalEnqueue(node -> {
			final TempVars tmp = TempVars.get();
			final Position p = e.get(Position.class);
			final Facing facing = e.get(Facing.class);
			final Vector3f currentLoc = p.get(tmp.vect1);
			final Quaternion currentRot = facing.get(tmp.quat1);
			// float blend = 1f * tpf;
			// currentRot.nlerp(s.getLocalRotation(), blend);
			// currentLoc.interpolateLocal(s.getLocalTranslation(), blend);
			if (s.getControl(InterpolationControl.class) == null)
			{
				s.addControl(new InterpolationControl());
			}
			s.getControl(InterpolationControl.class).setTarget(
					currentLoc.clone(),
					currentRot.clone());
			// s.setLocalTranslation(currentLoc);
			// s.setLocalRotation(currentRot);
			final Scale scale = e.get(Scale.class);
			s.setLocalScale(scale.get(tmp.vect2));
			tmp.release();
		});
	}

	protected void updateModels(final Set<Entity> set, float tpf)
	{

		for (final Entity e : set)
		{
			final Spatial s = getState(SceneGraphSystem.class).getSpatial(e.getId());
			if (s == null)
			{
				log.error("Model not found for updated entity:" + e);
				continue;
			}
			updateModelSpatial(e, s, tpf);
		}
	}

	protected void refreshModels(float tpf)
	{
		if (entities.applyChanges())
		{
			updateModels(entities.getAddedEntities(), tpf);
			updateModels(entities.getChangedEntities(), tpf);
		}
	}

}