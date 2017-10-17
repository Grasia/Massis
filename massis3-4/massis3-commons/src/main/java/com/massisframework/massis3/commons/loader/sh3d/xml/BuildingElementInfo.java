package com.massisframework.massis3.commons.loader.sh3d.xml;

public class BuildingElementInfo {
	protected String levelId;
	protected String massisGID;
	protected String spatialId;

	public BuildingElementInfo(final String levelId, final String massisGID,
			final String spatialId)
	{
		this.levelId = levelId;
		this.massisGID = massisGID;
		this.spatialId = spatialId;
	}

	public String getLevelId()
	{
		return levelId;
	}

	public void setLevelId(final String levelId)
	{
		this.levelId = levelId;
	}

	public String getMassisGID()
	{
		return massisGID;
	}

	public void setMassisGID(final String massisGID)
	{
		this.massisGID = massisGID;
	}

	public String getSpatialId()
	{
		return spatialId;
	}

	public void setSpatialId(final String spatialId)
	{
		this.spatialId = spatialId;
	}

}
