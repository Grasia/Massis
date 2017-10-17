package com.massisframework.massis3.commons.loader.sh3d.xml.model;

import java.util.List;

public interface FurnitureXML {

	String getModel();

	String getName();

	LevelXML getLevel();

	String getHeight();

	String getDepth();

	String getX();

	String getY();

	String getAngle();

	Object getModelMirrored();

	String getWidth();

	String getElevation();

	List<MaterialXML> getMaterial();

	float[][] getModelRotationAsMatrix();

}
