package com.massisframework.massis3.commons.spatials;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;
import com.jme3.util.TempVars;
import com.massisframework.massis3.commons.collections.CollectionsFactory;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class Octree<T> {

	private final Function<T, BoundingBox> boundFunction;
	private final Class<T> elemType;
	private final Octree<T>.OctreeNode root;
	private final int maxObjectsPerNode;
	private final Map<T, List<Octree<T>.OctreeNode>> nodesIn;

	public Octree(
			final BoundingBox boundary, final Class<T> elemType,
			final int maxObjectsPerNode,
			final Function<T, BoundingBox> boundFunction)
	{
		this.elemType = elemType;
		this.boundFunction = boundFunction;
		this.root = new OctreeNode(boundary, null);
		this.maxObjectsPerNode = maxObjectsPerNode;
		this.nodesIn = new HashMap<>();
	}

	public void remove(final T obj)
	{
		this.remove_internal(obj);
	}

	public boolean insert(final T obj)
	{
		List<Octree<T>.OctreeNode> containers = remove_internal(obj);
		if (containers == null)
		{
			containers = new ArrayList<>();
			this.nodesIn.put(obj, containers);
		}
		containers.clear();
		this.root.insert(obj, containers);
		// containers.removeIf(c -> {
		//
		// boolean rem = false;
		// BoundingBox objBounds = boundFunction.apply(obj);
		// TempVars tmp = TempVars.get();
		// if (c.bounds.contains(objBounds.getCenter()))
		// {
		// rem = true;
		// }
		// tmp.release();
		// return rem;
		// });
		return true;

	}

	public void queryRange(final BoundingBox range, final Consumer<T> action)
	{
		this.root.queryRange(range, action);
	}

	public void queryNodes(final Consumer<BoundingBox> action)
	{
		this.root.queryNodes(node -> {
			if (!node.objects.isEmpty())
				action.accept(node.bounds);
		});
	}

	public void queryNodes(final BiConsumer<BoundingBox, T> action)
	{
		this.root.queryNodes(node -> {
			if (!node.objects.isEmpty())
			{
				for (final T obj : node.objects)
				{
					action.accept(node.bounds, obj);
				}

			}
		});
	}

	private List<Octree<T>.OctreeNode> remove_internal(final T obj)
	{
		final List<Octree<T>.OctreeNode> containers = this.nodesIn.remove(obj);
		if (containers != null)
		{
			containers.forEach(c -> c.removeObject(obj));
		}
		return containers;
	}

	private class OctreeNode {

		private final List<T> objects;
		private OctreeNode children[];
		private final BoundingBox bounds;
		private final Octree<T>.OctreeNode parent;

		public OctreeNode(final BoundingBox boundary, final OctreeNode parent)
		{
			this.parent = parent;
			this.bounds = boundary;
			this.objects = CollectionsFactory.createList(elemType);
		}

		public void removeObject(final T obj)
		{
			if (!this.objects.remove(obj))
			{
				throw new RuntimeException();
			}
		}

		public boolean insert(final T object, final List<OctreeNode> containers)
		{
			// if (!bounds.intersects(boundFunction.apply(object)))
			// {
			// return false;
			// }
			if (!bounds.contains(boundFunction.apply(object).getCenter()))
			{
				return false;
			}

			if (children == null)
			{
				if (objects.size() > maxObjectsPerNode)
				{
					final List<T> objectsCP = CollectionsFactory
							.createList(elemType);
					objectsCP.add(object);
					objectsCP.addAll(this.objects);
					objectsCP.forEach(Octree.this::remove);
					subdivide();
					objectsCP.forEach(ocp -> this.insert(ocp, containers));

				} else
				{
					containers.add(this);
					objects.add(object);
				}
			} else
			{
				final int csize = containers.size();
				for (final OctreeNode octree : children)
				{
					octree.insert(object, containers);
				}
				if (containers.size() == csize)
				{
					containers.add(this);
					objects.add(object);
				}
			}

			return true;
		}

		public void queryRange(final BoundingBox range,
				final Consumer<T> action)
		{
			for (final T obj : objects)
			{
				if (range.intersects(boundFunction.apply(obj)))
				{
					action.accept(obj);
				}
			}
			if (children != null)
			{
				for (final OctreeNode octree : children)
				{
					if (octree.bounds.intersects(range))
					{
						octree.queryRange(range, action);
					}
				}
			}
		}

		public void queryNodes(final Consumer<OctreeNode> action)
		{
			action.accept(this);
			if (this.children != null)
			{
				for (final OctreeNode child : children)
				{
					child.queryNodes(action);
				}
			}
		}

		private void subdivide()
		{
			if (children != null)
			{
				throw new IllegalStateException("Already subdivided");
			}
			// System.out.println("Subdividing" + boundary);

			final TempVars tmp = TempVars.get();
			final Vector3f b_max = getBounds().getMax(tmp.vect1);
			final Vector3f b_min = getBounds().getMin(tmp.vect2);
			final Vector3f dim = b_max.subtract(b_min, tmp.vect3);
			final Vector3f half = dim.mult(0.5f, tmp.vect4);
			final Vector3f newHalf = half.mult(0.5f, tmp.vect5).clone();
			tmp.release();

			final BoundingBox[] newBounds = {
					new BoundingBox(
							new Vector3f(-newHalf.getX(), -newHalf.getY(),
									-newHalf.getZ()),
							half),
					new BoundingBox(
							new Vector3f(-newHalf.getX(), -newHalf.getY(),
									newHalf.getZ()),
							half),
					new BoundingBox(
							new Vector3f(-newHalf.getX(), newHalf.getY(),
									-newHalf.getZ()),
							half),
					new BoundingBox(
							new Vector3f(-newHalf.getX(), newHalf.getY(),
									newHalf.getZ()),
							half),
					new BoundingBox(
							new Vector3f(newHalf.getX(), -newHalf.getY(),
									-newHalf.getZ()),
							half),
					new BoundingBox(
							new Vector3f(newHalf.getX(), -newHalf.getY(),
									newHalf.getZ()),
							half),
					new BoundingBox(new Vector3f(newHalf.getX(), newHalf.getY(),
							-newHalf.getZ()), half),
					new BoundingBox(new Vector3f(newHalf.getX(), newHalf.getY(),
							newHalf.getZ()), half) };

			final Vector3f center = getBounds().getCenter();
			children = new Octree.OctreeNode[8];
			for (int i = 0; i < children.length; i++)
			{
				// newBounds[i].create();
				// newBounds[i].setPosition(
				// Vector3f.add(center, newBounds[i].getPosition(), null));
				newBounds[i].setCenter(newBounds[i].getCenter().add(center));
				children[i] = new Octree.OctreeNode(newBounds[i], this);
				// graphics.ThreeDGraphicsManager.getInstance().addGraphic3D(new
				// BoundingBoxGraphic(newBounds[i]), 0);
			}

		}

		public BoundingBox getBounds()
		{
			return bounds;
		}

	}
}