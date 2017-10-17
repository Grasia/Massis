package com.massisframework.massis3.commons.pathfinding.navmesh.impl;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;

public class DiscardQueue<E> implements Deque<E> {

	private final Deque<E> deque;
	private final int capacity;

	public DiscardQueue(final int capacity)
	{
		this.deque = new ArrayDeque<>(capacity);
		this.capacity = capacity;
	}

	@Override
	public boolean isEmpty()
	{
		return this.deque.isEmpty();
	}

	@Override
	public Object[] toArray()
	{
		return this.deque.toArray();
	}

	@Override
	public <T> T[] toArray(final T[] a)
	{
		return this.deque.toArray(a);
	}

	@Override
	public boolean containsAll(final Collection<?> c)
	{
		return this.deque.containsAll(c);
	}

	@Override
	public boolean addAll(final Collection<? extends E> c)
	{
		for (final E e : c)
		{
			this.addFirst(e);
		}
		return true;
	}

	@Override
	public boolean removeAll(final Collection<?> c)
	{
		return this.deque.removeAll(c);
	}

	@Override
	public boolean retainAll(final Collection<?> c)
	{
		return this.deque.retainAll(c);
	}

	@Override
	public void clear()
	{
		this.deque.clear();

	}

	@Override
	public void addFirst(final E e)
	{
		if (this.capacity == this.deque.size())
		{
			this.deque.removeLast();
		}
		this.deque.addFirst(e);

	}

	@Override
	public void addLast(final E e)
	{
		if (this.capacity == this.deque.size())
		{
			this.deque.removeFirst();
		}
		this.deque.addLast(e);
	}

	@Override
	public boolean offerFirst(final E e)
	{
		this.addFirst(e);
		return true;
	}

	@Override
	public boolean offerLast(final E e)
	{
		this.addLast(e);
		return true;
	}

	@Override
	public E removeFirst()
	{
		return this.deque.removeFirst();
	}

	@Override
	public E removeLast()
	{
		return this.deque.removeLast();
	}

	@Override
	public E pollFirst()
	{
		return this.deque.pollFirst();
	}

	@Override
	public E pollLast()
	{
		return this.deque.pollLast();
	}

	@Override
	public E getFirst()
	{
		return this.deque.getFirst();
	}

	@Override
	public E getLast()
	{
		return this.deque.removeLast();
	}

	@Override
	public E peekFirst()
	{
		return this.deque.peekFirst();
	}

	@Override
	public E peekLast()
	{
		return this.deque.peekLast();
	}

	@Override
	public boolean removeFirstOccurrence(final Object o)
	{
		return this.deque.removeFirstOccurrence(o);
	}

	@Override
	public boolean removeLastOccurrence(final Object o)
	{
		return this.deque.removeLastOccurrence(o);
	}

	@Override
	public boolean add(final E e)
	{
		this.addFirst(e);
		return true;
	}

	@Override
	public boolean offer(final E e)
	{
		this.addFirst(e);
		return true;
	}

	@Override
	public E remove()
	{
		return this.deque.remove();
	}

	@Override
	public E poll()
	{
		return this.deque.poll();
	}

	@Override
	public E element()
	{
		return this.deque.element();
	}

	@Override
	public E peek()
	{
		return this.deque.peek();
	}

	@Override
	public void push(final E e)
	{
		this.addFirst(e);
	}

	@Override
	public E pop()
	{
		return this.deque.pop();
	}

	@Override
	public boolean remove(final Object o)
	{
		return this.deque.remove(o);
	}

	@Override
	public boolean contains(final Object o)
	{
		return this.deque.contains(o);
	}

	@Override
	public int size()
	{
		return this.deque.size();
	}

	@Override
	public Iterator<E> iterator()
	{
		return new UnmodifiableIterator<>(this.deque.iterator());
	}

	@Override
	public Iterator<E> descendingIterator()
	{
		return new UnmodifiableIterator<>(this.deque.descendingIterator());
	}

	private static class UnmodifiableIterator<T> implements Iterator<T> {

		private final Iterator<T> it;

		public UnmodifiableIterator(final Iterator<T> it)
		{
			this.it = it;
		}

		@Override
		public boolean hasNext()
		{
			return it.hasNext();
		}

		@Override
		public T next()
		{
			return it.next();
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException("remove Not supported");
		}

	}

	@Override
	public String toString()
	{
		return "DiscardQueue [deque=" + deque + "]";
	}

}
