package com.massisframework.massis3.commons.app.server;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

public interface InternalServerCamera extends ServerCamera {


	public void release();

	public Vector3f getUp();

	public Vector3f getLeft();

	public void setAxes(Quaternion q);

	public float getFrustumTop();

	public float getFrustumRight();

	public float getFrustumNear();

	public void setCamFrustumTop(float h);

	public void setCamFrustumBottom(float f);

	public void setCamFrustumLeft(float f);

	public void setCamFrustumRight(float w);

	public Vector3f getLeft(Vector3f vel);

	public void setMainCam(boolean b);
}
