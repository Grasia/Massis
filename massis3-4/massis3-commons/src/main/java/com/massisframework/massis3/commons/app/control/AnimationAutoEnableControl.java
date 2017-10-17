package com.massisframework.massis3.commons.app.control;

import com.jme3.animation.AnimControl;
import com.jme3.bounding.BoundingVolume;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import com.massisframework.massis3.commons.spatials.Spatials;

public class AnimationAutoEnableControl extends AbstractControl {

	float timeInvisible = 0f;
	float disableTime;
	private final float distance;
	private float distanceToCamera = 0;

	public AnimationAutoEnableControl(final float disableTime,
			final float distance)
	{
		this.disableTime = disableTime;
		this.distance = distance;
	}

	@Override
	protected void controlUpdate(final float tpf)
	{
		final boolean enabled = timeInvisible < disableTime
				&& this.distanceToCamera <= this.distance;
		Spatials.getAllControls(getSpatial(), AnimControl.class)
				.forEach(ac -> ac.setEnabled(enabled));
		// Spatials.getFirstControl(getSpatial(),
		// AnimControl.class).setEnabled(enabled);
		timeInvisible += tpf;
		this.distanceToCamera = Float.MAX_VALUE;
	}

	@Override
	protected void controlRender(final RenderManager rm, final ViewPort vp)
	{
		this.timeInvisible = 0;
		final float distCam = this.getSpatial()
				.getWorldBound().getCenter()
				.distance(vp.getCamera().getLocation());
		this.distanceToCamera = Math.min(distCam, this.distanceToCamera);
	}

	protected boolean isVisibleByViewPort(final ViewPort vp)
	{
		final BoundingVolume bv = getSpatial().getWorldBound();
		final int planeState = vp.getCamera().getPlaneState();
		vp.getCamera().setPlaneState(0);
		final Camera.FrustumIntersect result = vp.getCamera().contains(bv);
		vp.getCamera().setPlaneState(planeState);
		return result == Camera.FrustumIntersect.Inside;
	}

}
