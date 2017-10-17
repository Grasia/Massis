package com.massisframework.massis3.sposh;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.jme3.math.Vector3f;

import cz.cuni.amis.pogamut.sposh.context.Context;

@SuppressWarnings("unchecked")
public class Massis3Context<AGENT extends Massis3Agent> extends Context<AGENT> {

	private Map<ContextKey, Object> contextProperties;

	protected Massis3Context(AGENT bot)
	{
		super(String.valueOf(bot.getID()), bot);
		this.contextProperties = new ConcurrentHashMap<>();
	}

	public <T> T getContextValue(ContextKey key)
	{
		return (T) this.contextProperties.get(key);
	}

	public <T> T getContextValue(ContextKey key, T def)
	{
		return (T) this.contextProperties.getOrDefault(key, def);
	}

	public void setContextValue(ContextKey key, Object value)
	{
		if (value == null)
		{
			this.contextProperties.remove(key);
		}
		this.contextProperties.put(key, value);
	}

	public void setMovementTarget(Vector3f target)
	{
		this.setContextValue(ContextKey.MOVEMENT_TARGET, target.clone());
	}

	public void setMovementTarget(double x, double y, double z)
	{
		this.setContextValue(ContextKey.MOVEMENT_TARGET,
				new Vector3f((float) x, (float) y, (float) z));
	}

	public Vector3f getMovementTarget(Vector3f store)
	{
		Vector3f movementTarget = this.getContextValue(ContextKey.MOVEMENT_TARGET, Vector3f.NAN);
		return store.set(
				movementTarget.x,
				movementTarget.y,
				movementTarget.z);
	}
}
