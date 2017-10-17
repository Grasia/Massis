package com.massisframework.massis3.commons.app.system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.massisframework.massis3.commons.app.system.debug.BasicColor;
import com.massisframework.massis3.commons.spatials.Spatials;

public class DebugShapesSystem extends AbstractMassisSystem {

	private static final Logger log = LoggerFactory.getLogger(DebugShapesSystem.class);

	public enum DebugShapeType {
		LINE, BOX, SPHERE, CIRCLE;
	}

	private Map<DebugShapeType, List<Spatial>> poolNode;
	private Node sceneNode;
	private boolean attached = false;

	@Override
	public void simpleInitialize()
	{
		this.poolNode = new HashMap<>();
		this.sceneNode = new Node(getClass().getName());

		for (final DebugShapeType type : DebugShapeType.values())
		{
			final Node node1 = new Node(type.name());
			this.sceneNode.attachChild(node1);
			this.poolNode.put(type, new ArrayList<>());
		}
	}

	public void drawLine(final BasicColor color, final Vector3f from,
			final Vector3f to)
	{

		if (!Vector3f.isValidVector(from))
		{
			log.error("Line not valid. Skipping drawing");
			return;
		}
		if (!Vector3f.isValidVector(to))
		{
			log.error("Line not valid. Skipping drawing");
			return;
		}
		final Spatial line = addToScene(DebugShapeType.LINE);
		color.applyTo(line);
		Spatials.modifyLineGeometry(from, to, line);
	}

	public void drawBox(final BasicColor color, final Vector3f center,
			final float xScale,
			final float yScale, final float zScale)
	{
		this.drawBox(color, center, xScale, yScale, zScale, false);
	}

	public void drawBox(final BasicColor color, final Vector3f center,
			final float xScale,
			final float yScale, final float zScale, final boolean wired)
	{

		final Spatial box = addToScene(DebugShapeType.BOX);
		color.applyTo(box, wired);
		box.setLocalTranslation(center);
		box.setLocalScale(xScale, yScale, zScale);
	}

	public void drawCircle(final BasicColor color, final Vector3f center,
			final float radius)
	{

		drawCircle(color, center, radius, false);
	}

	public void drawCircle(
			final BasicColor color,
			final Vector3f center,
			final float radius,
			final boolean wired)
	{

		if (!Vector3f.isValidVector(center))
		{
			throw new RuntimeException("Center not valid: " + center);
		}
		final Spatial circle = addToScene(DebugShapeType.CIRCLE);
		color.applyTo(circle, wired);
		circle.setLocalScale(radius);
		circle.setLocalTranslation(center);
	}

	public void drawSphere(final BasicColor color, final Vector3f center,
			final float radius)
	{
		drawSphere(color, center, radius, radius, radius, false);
	}

	public void drawSphere(final BasicColor color, final Vector3f center,
			final float radius,
			final boolean wired)
	{
		drawSphere(color, center, radius, radius, radius, wired);
	}

	public void drawSphere(final BasicColor color, final Vector3f center,
			final float xScale,
			final float yScale, final float zScale)
	{
		drawSphere(color, center, xScale, yScale, zScale, false);
	}

	public void drawSphere(final BasicColor color, final Vector3f center,
			final float xScale,
			final float yScale, final float zScale, final boolean wired)
	{

		if (!Vector3f.isValidVector(center))
		{
			log.error("CENTER NOT VALID " + center);
			return;
		}
		final Spatial sphere = addToScene(DebugShapeType.SPHERE);
		color.applyTo(sphere, wired);
		sphere.setLocalTranslation(center);
		sphere.setLocalScale(xScale, yScale, zScale);
	}

	public void drawBox(final BasicColor color, final Vector3f center,
			final float scale)
	{
		this.drawBox(color, center, scale, scale, scale);
	}

	private Spatial addToScene(final DebugShapeType type)
	{

		final List<Spatial> pool = this.poolNode.get(type);
		if (pool.isEmpty())
		{
			final Spatial shape = this.createShape(type);
			pool.add(shape);
			shape.setCullHint(CullHint.Always);
			((Node) this.sceneNode.getChild(type.name())).attachChild(shape);
		}
		final Spatial child = pool.remove(pool.size() - 1);
		child.setCullHint(CullHint.Never);
		return child;
	}

	private Spatial createShape(final DebugShapeType type)
	{
		switch (type)
		{
		case LINE:
			return Spatials.createLine(Vector3f.ZERO, Vector3f.UNIT_X);
		case BOX:
			return Spatials.createBox(Vector3f.ZERO);
		case SPHERE:
			return Spatials.createSphere(Vector3f.ZERO);
		case CIRCLE:
			return Spatials.createCircle(Vector3f.ZERO);
		default:
			throw new UnsupportedOperationException(
					"Cannot create debug shape of type " + type
							+ ". Not implemented");

		}
	}

	@Override
	public void postRender()
	{
		for (final Spatial debugSp : this.sceneNode.getChildren())
		{
			final DebugShapeType type = DebugShapeType
					.valueOf(debugSp.getName());
			final Node debugNode = (Node) debugSp;
			debugNode.getChildren()
					.forEach(c -> c.setCullHint(CullHint.Always));
			this.poolNode.get(type).addAll(debugNode.getChildren());
			debugNode.detachAllChildren();
		}
	}

	@Override
	public void simpleCleanup()
	{
		this.sceneNode.removeFromParent();
		this.sceneNode.detachAllChildren();
		this.poolNode.values().forEach(List::clear);
		this.poolNode.clear();
	}

	@Override
	protected void onDisable()
	{

	}

	@Override
	protected void onEnable()
	{

	}

	@Override
	public void update()
	{

	}

	@Override
	public void graphicalUpdate(Node systemNode)
	{
		if (!attached)
		{
			attached = true;
			systemNode.attachChild(systemNode);
		}
	}
}
