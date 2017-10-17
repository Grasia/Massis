package com.massisframework.massis3.commons.loader.sh3d.xml;

import java.util.Map;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

public class FurnitureObjectInfo extends BuildingElementInfo {

	protected String name;
	protected Vector3f translation;
	protected Quaternion rotation;
	protected Vector3f scale;
	protected Map<String, Integer> materials;
	protected boolean isDoorOrWindow;

	public FurnitureObjectInfo(final String levelId, final String massisGID,
			final String spatialId, final String name,
			final Vector3f translation,
			final Quaternion rotation,
			final Vector3f scale, final boolean isDoorOrWindow,
			final Map<String, Integer> materials)
	{
		super(levelId, massisGID, spatialId);
		this.name = name;
		this.isDoorOrWindow = isDoorOrWindow;
		this.translation = translation;
		this.rotation = rotation;
		this.scale = scale;
		this.materials = materials;
	}

	public Vector3f getTranslation()
	{
		return translation;
	}

	public Quaternion getRotation()
	{
		return rotation;
	}

	public Vector3f getScale()
	{
		return scale;
	}

	public Map<String, Integer> getMaterials()
	{
		return materials;
	}

	public boolean isDoorOrWindow()
	{
		return isDoorOrWindow;
	}

	public String getName()
	{
		return name;
	}

}
