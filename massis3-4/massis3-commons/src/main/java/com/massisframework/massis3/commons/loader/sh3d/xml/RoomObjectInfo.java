package com.massisframework.massis3.commons.loader.sh3d.xml;

import java.util.List;

public class RoomObjectInfo extends BuildingElementInfo {

	/* ROOM INFORMATION */

	protected String name;
	protected List<float[]> points;

	public RoomObjectInfo(final String levelId, final String massisGID,
			final String spatialId,
			final String name, final List<float[]> points)
	{
		super(levelId, massisGID, spatialId);
		this.name = name;
		this.points = points;
	}

	public String getName()
	{
		return name;
	}

	public void setName(final String name)
	{
		this.name = name;
	}

	public List<float[]> getPoints()
	{
		return points;
	}

	public void setPoints(final List<float[]> points)
	{
		this.points = points;
	}

}
