package com.massisframework.massis3.core.jfx.hud;

import com.jme3.app.state.AppStateManager;

public interface JFXJmeController {

	public void jmeInitialize(AppStateManager stateManager);

	public void jmeUpdate(float tpf);

	/**
	 * This method needs to be overridden by extending classes. It is going to
	 * be called in every frame while the {@code AnimationTimer} is active.
	 *
	 * @param now
	 *            The timestamp of the current frame given in nanoseconds. This
	 *            value will be the same for all {@code AnimationTimers} called
	 *            during one frame.
	 */
	public void jfxUdate(long now);
}
