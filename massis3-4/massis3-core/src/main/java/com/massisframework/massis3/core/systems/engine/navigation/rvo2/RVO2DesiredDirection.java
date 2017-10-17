package com.massisframework.massis3.core.systems.engine.navigation.rvo2;

import com.simsilica.es.EntityComponent;

public class RVO2DesiredDirection implements EntityComponent {

	private float x, z;

	public RVO2DesiredDirection(final float x, final float z)
	{
		this.x = x;
		this.z = z;
	}

	public float getX()
	{
		return x;
	}

	public void setX(final float x)
	{
		this.x = x;
	}

	public float getZ()
	{
		return z;
	}

	public void setZ(final float z)
	{
		this.z = z;
	}
}
