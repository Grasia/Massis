package com.massisframework.massis3.commons.collections.map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.math.Vector3f;

public class ConstrainedPointSet {

	private static final Logger log = LoggerFactory.getLogger(ConstrainedPointSet.class);
	private ConstrainedPointMap<Object> map;
	private static final Object DUMMY = new Object();

	public ConstrainedPointSet(final Vector3f min, final Vector3f max, float cellSize)
	{
		this.map = new ConstrainedPointMap<>(Object.class, min, max, cellSize);
	}

	
	
	public boolean contains(Vector3f v)
	{
		return map.containsKey(v);
	}

	public boolean contains(float x, float y, float z)
	{
		return map.containsKey(x, y, z);
	}

}
