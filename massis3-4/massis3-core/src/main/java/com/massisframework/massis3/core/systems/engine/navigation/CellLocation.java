package com.massisframework.massis3.core.systems.engine.navigation;

import com.jme3.math.Vector3f;
import com.simsilica.es.EntityComponent;

public class CellLocation implements EntityComponent {

	private boolean valid = true;
	private final int cellId;
	private final Vector3f pointInCell;

	public CellLocation(final int cellId, final boolean valid,
			final Vector3f pointInCell)
	{
		this.cellId = cellId;
		this.valid = valid;
		this.pointInCell = pointInCell;
	}

	public boolean isValid()
	{
		return valid;
	}

	public CellLocation setValid(final boolean valid)
	{
		this.valid = valid;
		return this;
	}

	public int getCellId()
	{
		return cellId;
	}

	public Vector3f getPointInCell()
	{
		return pointInCell;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + cellId;
		result = prime * result + (valid ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CellLocation other = (CellLocation) obj;
		if (cellId != other.cellId)
			return false;
		if (valid != other.valid)
			return false;
		return true;
	}
}
