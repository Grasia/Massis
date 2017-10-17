package com.massisframework.massis3.commons.pool;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SimplePool<T> {

	private List<T> objects;
	private int used;
	private Supplier<T> generator;

	public SimplePool(final Supplier<T> generator)
	{
		this.generator = generator;
		this.objects = new ArrayList<>();
		this.used = 0;
	}

	public SimplePool(final Class<T> type)
	{
		this(() -> {
			try
			{
				return type.newInstance();
			} catch (InstantiationException | IllegalAccessException e)
			{
				throw new RuntimeException();
			}
		});
	}

	public T get()
	{
		if (used == this.objects.size())
		{
			this.objects.add(generator.get());
		}
		final T item = this.objects.get(used);
		used++;
		return item;
	}

	public void release()
	{
		this.used = 0;
	}

}
