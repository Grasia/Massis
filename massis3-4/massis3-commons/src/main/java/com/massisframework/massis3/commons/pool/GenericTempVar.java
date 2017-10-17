package com.massisframework.massis3.commons.pool;

public class GenericTempVar {

	private final ReleaseAble r;

	public GenericTempVar(final ReleaseAble tempVars)
	{
		this.r = tempVars;
	}

	public void release()
	{
		this.r.release();
	}

}
