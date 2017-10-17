package com.massisframework.massis3.commons.collections;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.simsilica.es.EntityId;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public class EntityIdObjectMap<T> {

	private Long2ObjectOpenHashMap<T> map;

	public EntityIdObjectMap()
	{
		this.map = new Long2ObjectOpenHashMap<>();
	}

	public int size()
	{
		return map.size();
	}

	public boolean isEmpty()
	{
		return map.isEmpty();
	}

	public boolean containsKey(EntityId key)
	{
		return map.containsKey(key.getId());
	}

	public boolean containsValue(T value)
	{
		return map.containsValue(value);
	}

	public T get(EntityId key)
	{
		return map.get(key.getId());
	}

	public T put(EntityId key, T value)
	{
		return map.put(key.getId(), value);
	}

	public T remove(EntityId key)
	{
		return map.remove(key.getId());
	}

	public void putAll(Map<? extends EntityId, ? extends T> m)
	{
		m.forEach((eid, t) -> this.put(eid, t));
	}

	public void clear()
	{
		map.clear();
	}

	public Set<Long> keySet()
	{
		return map.keySet();
	}
	
	public Collection<T> values()
	{
		return map.values();
	}

	public Set<Map.Entry<Long, T>> entrySet()
	{
		return map.entrySet();
	}

	@Override
	public boolean equals(Object o)
	{
		return map.equals(o);
	}

	@Override
	public int hashCode()
	{
		return map.hashCode();
	}

}
