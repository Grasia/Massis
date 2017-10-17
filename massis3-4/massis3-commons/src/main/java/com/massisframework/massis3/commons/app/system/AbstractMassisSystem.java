package com.massisframework.massis3.commons.app.system;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;

import org.slf4j.LoggerFactory;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import com.massisframework.massis3.commons.app.server.AppSystemManager;
import com.massisframework.massis3.commons.app.server.MassisSystem;

public abstract class AbstractMassisSystem implements MassisSystem {

	private ConcurrentLinkedDeque<Consumer<Node>> tasks;
	private AppSystemManager stateManager;
	private float tpf;
	private float time;
	private boolean firstTick = true;
	private Set<Class<? extends MassisSystem>> accessibleAppStates;

	public AbstractMassisSystem()
	{
		this.tasks = new ConcurrentLinkedDeque<>();
		this.accessibleAppStates = new HashSet<>();
		RequiresSystems ann = this.getClass().getAnnotation(RequiresSystems.class);
		if (ann != null)
		{
			for (Class<? extends MassisSystem> accessible : ann.value())
			{
				this.accessibleAppStates.add(accessible);
			}
		}
	}

	@Override
	public void stateAttached(AppSystemManager stateManager)
	{
		this.stateManager = stateManager;
	}

	@Override
	public final void initialize(final AppSystemManager stateManager)
	{
		this.stateManager = stateManager;
		if (LoggerFactory.getLogger(getClass()).isInfoEnabled())
		{
			LoggerFactory.getLogger(getClass()).info("initializing state");
		}

		simpleInitialize();
		initialized = true;
	}

	protected boolean initialized = false;
	private boolean enabled = true;

	public boolean isInitialized()
	{
		return initialized;
	}

	@Override
	public boolean isEnabled()
	{
		return enabled;
	}

	protected abstract void simpleInitialize();

	protected abstract void onDisable();

	protected abstract void onEnable();

	protected void simpleCleanup()
	{
	}

	public abstract void update();

	public void graphicalUpdate(Node systemNode)
	{
	}

	@Override
	public final void graphicalUpdate(Node systemNode, float tpf)
	{
		this.graphicalUpdate(systemNode);
	}

	public void runPendingTasks(Node systemNode)
	{
		while (this.tasks.size() > 0)
		{
			this.tasks.poll().accept(systemNode);
		}
	}

	@Override
	public final void update(float tpf)
	{
		this.tpf = tpf;
		this.time = this.stateManager.time();
		if (this.firstTick)
		{
			this.firstTick = false;
			if (this.isEnabled())
			{
				this.onEnable();
			}
		} else
		{
			this.update();
		}
	}

	public AssetManager getAssetManager()
	{
		return this.stateManager.getDefaultAssetManager();
	}

	public float tpf()
	{
		return this.tpf;
	}

	public float time()
	{
		return this.time;
	}

	@Override
	public final void cleanup()
	{
		this.setEnabled(false);
		this.simpleCleanup();
	}

	protected <T extends MassisSystem> T getState(final Class<T> stateClass)
	{
		if (this.accessibleAppStates.contains(stateClass))
			return this.stateManager.getSystem(stateClass);
		else
			throw new IllegalArgumentException("State type " + stateClass.getName()
					+ " is not declared in @" + RequiresSystems.class.getSimpleName());
	}

	@Override
	public final void setEnabled(boolean enabled)
	{
		if (this.isInitialized())
		{
			if (enabled != this.isEnabled())
			{
				this.enabled = enabled;
				if (this.isEnabled())
				{
					this.onEnable();
				} else
				{
					this.onDisable();
					this.stateManager.enqueue(() -> {
						
					});
				}

			}
		} else
		{
			this.enabled = enabled;
		}
	}

	protected void graphicalEnqueue(Consumer<Node> action)
	{
		this.tasks.add(action);
	}

	public void postRender()
	{

	}

}
