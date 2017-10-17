package com.massisframework.massis3.commons.pathfinding.navmesh.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class ICellCache {

	private LoadingCache<Integer, CellLinkCache> cache;

	public ICellCache(final int maxSize, final int cellLinkCapacity)
	{
		this.cache = CacheBuilder
				.newBuilder()
				.concurrencyLevel(1)
				.maximumSize(maxSize)
				.expireAfterAccess(1, TimeUnit.MINUTES)
				.build(new CacheLoader<Integer, CellLinkCache>() {
					@Override
					public CellLinkCache load(final Integer key)
							throws Exception
					{
						return new CellLinkCache(cellLinkCapacity);
					}
				});
	}

	public int getLinkIndex(final int originCellId, final int targetCellId)
	{
		try
		{
			return this.cache.get(originCellId).getLinkIndex(targetCellId);
		} catch (final ExecutionException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void addWay(final int originCellId, final int targetCellId,
			final int linkIndex)
	{
		try
		{
			this.cache.get(originCellId).addWay(targetCellId, linkIndex);
		} catch (final ExecutionException e)
		{
			throw new RuntimeException(e);
		}
	}
}
