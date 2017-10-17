package com.massisframework.massis3.commons.loader.sh3d.xml;

import java.util.List;

public class WallObjectInfo extends BuildingElementInfo {

	protected float heightAtStart;
	protected float heightAtEnd;
	protected float thickness;

	protected List<float[]> points;

	public WallObjectInfo(
			final String levelId,
			final String massisGID,
			final String spatialId,
			final float heightAtStart,
			final float heightAtEnd,
			final float thickness,
			final List<float[]> points)
	{

		super(levelId, massisGID, spatialId);
		this.heightAtStart = heightAtStart;
		this.heightAtEnd = heightAtEnd;
		this.thickness = thickness;
		this.points = points;
	}

	public float getHeightAtStart()
	{
		return heightAtStart;
	}

	public float getHeightAtEnd()
	{
		return heightAtEnd;
	}

	public float getThickness()
	{
		return thickness;
	}

	public List<float[]> getPoints()
	{
		return points;
	}

}
