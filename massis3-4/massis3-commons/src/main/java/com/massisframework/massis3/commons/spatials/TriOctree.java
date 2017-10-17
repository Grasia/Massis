package com.massisframework.massis3.commons.spatials;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;

public class TriOctree<T> {

	public static final class OctreeNode<T> {

		public BoundingBox boundingBox;

		public final List<OctreeNode<T>> children = new ArrayList<>();

		public final List<T> data;

		private OctreeNode<T> parent;

		private Function<T, Vector3f> bbFn;

		public OctreeNode(final Vector3f center, final float halfWidth,
				final OctreeNode<T> parent, final Function<T, Vector3f> bbFn)
		{
			this(new BoundingBox(center, halfWidth, halfWidth, halfWidth),
					parent, bbFn);

		}

		public OctreeNode(final BoundingBox bb, final OctreeNode<T> parent,
				final Function<T, Vector3f> bbFn)
		{
			Objects.requireNonNull(bb);
			this.parent = parent;
			this.boundingBox = new BoundingBox(bb);
			this.data = new ArrayList<>();
			this.bbFn = bbFn;
		}

		public void visitData(final Consumer<T> visitor)
		{
			this.data.forEach(visitor::accept);
		}

		public OctreeNode<T> findNode(final T item)
		{

			if (!this.contains(item))
				return null;

			return children.stream()
					.map(c -> c.findNode(item))
					.filter(el -> el != null)
					.findFirst()
					.orElseGet(() -> this);
		}

		public OctreeNode<T> findNode(final Vector3f item)
		{

			if (!this.contains(item))
			{
				return null;
			}
			final OctreeNode<T> ret = children.stream()
					.map(c -> c.findNode(item))
					.filter(el -> el != null)
					.findFirst()
					.orElseGet(() -> this);
			return ret;
		}

		public T findAny()
		{
			if (this.data == null || this.data.isEmpty())
			{
				for (final OctreeNode<T> c : children)
				{
					final T item = c.findAny();
					if (item != null)
					{
						return item;
					}
				}
				return null;
			} else
			{
				return this.data.get(0);
			}
		}

		public boolean contains(final T bbc)
		{
			return this.contains(bbFn.apply(bbc));
		}

		public boolean contains(final Vector3f p)
		{
			return this.boundingBox.intersects(p);
		}

		public void visit(final Consumer<OctreeNode<T>> visitor)
		{
			visitor.accept(this);
			children.forEach(c -> c.visit(visitor));
		}

		public OctreeNode<T> add(final T obj, final int lvl)
		{
			if (this.children.isEmpty() && lvl<8)
			{
				this.split();
			}
			//if (lvl < 4)
			else
			{
				// OctreeNode<T> node = findNode(obj);
				for (final OctreeNode<T> c : this.children)
				{
					if (c.contains(obj))
					{
						final OctreeNode<T> res = c.add(obj, lvl + 1);
						return res;
					}
				}
			}

			this.data.add(obj);
			return this;
		}

		public void remove(final T obj)
		{
			this.data.remove(obj);

			for (final OctreeNode<T> c : this.children)
			{
				// if (c.contains(obj))
				{
					c.remove(obj);
				}
			}
			// if (!this.children.isEmpty())
			// {
			// for (OctreeNode<T> c : children)
			// {
			// c.remove(obj);
			// }
			// }
		}

		// private boolean intersectsWith(T item, BoundingBox bb)
		// {
		// TempVars tmp = TempVars.get();
		// BoundingBox objBB = bbFn.apply(item);
		// boolean ok = bb.contains(objBB.getCenter());// objBB.intersects(bb);
		// tmp.release();
		// return ok;
		// }

		// private boolean contains(T item, BoundingBox bb)
		// {
		// TempVars tmp = TempVars.get();
		// BoundingBox objBB = bbFn.apply(item);
		// Vector3f min = objBB.getMin(tmp.vect1);
		// Vector3f max = objBB.getMin(tmp.vect2);
		// boolean ok = bb.contains(min)
		// && bb.contains(max);
		// tmp.release();
		// return ok;
		// }

		public void split()
		{

			final Vector3f center = this.boundingBox.getCenter();

			final float nw = this.boundingBox.getXExtent() / 2;
			children.clear();
			IntStream.range(0, 8)
					.mapToObj(i -> new Vector3f(
							(i >> 2 & 1) * 2 - 1,
							(i >> 1 & 1) * 2 - 1,
							(i >> 0 & 1) * 2 - 1)
									.multLocal(nw)
									.addLocal(center))
					.map(nc -> new OctreeNode<T>(nc, nw, this, bbFn))
					.forEach(children::add);
			// data.forEach(obj -> {
			// children.stream()
			// .filter(child -> child.contains(obj))
			// .findFirst()
			// .orElseThrow(() -> new RuntimeException())
			// .add(obj);
			// });
			// data.clear();
		}

		@Override
		public String toString()
		{
			return "OctreeNode [data=" + data + "]";
		}
	}

	private final OctreeNode<T> root;

	public TriOctree(final BoundingBox box, final Function<T, Vector3f> bbFn)
	{
		this.root = new OctreeNode<>(box, null, bbFn);
	}

	private final Map<T, OctreeNode<T>> nodeMap = new HashMap<>();

	public void add(final T obj)
	{
		remove(obj);
		final OctreeNode<T> octNode = root.add(obj, 0);
		this.nodeMap.put(obj, octNode);
	}

	public void remove(final T obj)
	{
		final OctreeNode<T> node = this.nodeMap.remove(obj);
		if (node != null)
		{
			node.remove(obj);
		}
		// root.remove(obj);
	}

	private static class Wrapper<K> {
		K store;
		float distance;
		Vector3f nearest;
	}

	public void visit(final Consumer<OctreeNode<T>> visitor)
	{
		root.visit(visitor);
	}

	public List<T> nodeNeighbors(Vector3f point)
	{
		OctreeNode<T> node = root.findNode(point);
		if (node==null) return Collections.emptyList();
		while (node!=null && node.data.isEmpty())
		{
			node = node.parent;
		}
		if (node == null)
		{
			return Collections.emptyList();
		} else
		{
			return Collections.unmodifiableList(node.data);
		}
	}

	public void visitBoxes(final Consumer<BoundingBox> visitor)
	{
		this.visit((otn) -> visitor.accept(otn.boundingBox));
	}

	public void visitBoxesWithData(final Consumer<BoundingBox> visitor)
	{
		this.visit((otn) -> {
			if (otn.data != null && !otn.data.isEmpty())
				visitor.accept(otn.boundingBox);
		});
	}

	public void visitData(final Consumer<T> visitor)
	{
		this.visit(otn -> {
			if (otn.data != null || !otn.data.isEmpty())
			{
				otn.data.forEach(d -> visitor.accept(d));
			}
		});

	}

	public void visitDataBB(final BiConsumer<BoundingBox, T> visitor)
	{
		this.visit(otn -> {
			if (otn.data != null)
			{
				otn.data.forEach(d -> visitor.accept(otn.boundingBox, d));
			}
		});

	}

}