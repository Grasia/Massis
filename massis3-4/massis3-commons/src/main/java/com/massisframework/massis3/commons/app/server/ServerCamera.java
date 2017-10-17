package com.massisframework.massis3.commons.app.server;

import java.awt.image.BufferedImage;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

public interface ServerCamera {

	
	public boolean isReleased();
	
	public void attachScene(Spatial scene);

	public void detachScene(Spatial scene);

	// public void render();

	public BufferedImage getImage();

	public void setLocation(Vector3f vector3f);

	void setRotation(Quaternion rot);

	public String getId();

	int getHeight();

	public Vector3f getLocation();

	public Quaternion getRotation();

	public Vector3f getDirection(Vector3f store);

	public default Vector3f getDirection() {
		return getDirection(new Vector3f());
	}

	
	

}
