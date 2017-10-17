package com.massisframework.massis3.commons.pathfinding.navmesh;

public enum FindPathResult {
	/**
	 * The path has been correctly computed. The path returned is the complete
	 * path from start to goal
	 */
	COMPLETE_PATH_FOUND(true),
	/**
	 * The path has been correctly computed. The path is returned partially.
	 */
	PARTIAL_PATH_FOUND(true),
	/**
	 * The path has not been correctly computed. The value of the returned list
	 * is undefined.
	 */
	NOT_FOUND(false);

	private boolean success;

	FindPathResult(final boolean success)
	{
		this.success = success;
	}

	/**
	 * 
	 * @return if the path has been correctly computed.
	 */
	public boolean isSuccess()
	{
		return this.success;
	}
}
