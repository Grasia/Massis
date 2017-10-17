package com.massisframework.massis3.commons.spatials;

import com.jme3.math.Quaternion;

public interface QuaternionHolder {

	public float getX();

	public float getY();

	public float getZ();
	
	public float getW();

	public default Quaternion get()
	{
		return get(new Quaternion());
	}

	public default Quaternion get(Quaternion store)
	{
		return store.set(getX(), getY(), getZ(),getW());
	}
}
