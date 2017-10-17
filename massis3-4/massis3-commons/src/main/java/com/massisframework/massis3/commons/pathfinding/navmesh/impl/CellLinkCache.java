package com.massisframework.massis3.commons.pathfinding.navmesh.impl;

public class CellLinkCache {

	private final DiscardQueue<int[]> memory;

	public CellLinkCache(final int capacity)
	{
		this.memory = new DiscardQueue<>(capacity);
	}

	public void addWay(final int cellId, final int linkIndex)
	{
		// if (!contains(cellId, linkIndex))
		{
			memory.add(new int[] { cellId, linkIndex });
		}
	}

	public int getLinkIndex(final int cellId)
	{
		for (final int[] is : memory)
		{
			if (is[0] == cellId)
				return is[1];
		}
		return -1;
	}
}
