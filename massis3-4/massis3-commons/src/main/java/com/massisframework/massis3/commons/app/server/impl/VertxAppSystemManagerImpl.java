package com.massisframework.massis3.commons.app.server.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import com.jme3.util.SafeArrayList;
import com.massisframework.massis3.commons.app.server.AppSystemManager;
import com.massisframework.massis3.commons.app.server.InternalServerCamera;
import com.massisframework.massis3.commons.app.server.MassisSystem;
import com.massisframework.massis3.commons.app.server.ServerCamera;
import com.massisframework.massis3.commons.app.server.impl.multi.MultiCameraApp;
import com.massisframework.massis3.commons.app.system.SystemsTopologicalSorter;
import com.massisframework.massis3.commons.spatials.Materials;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

/**
 * Heavily based on JME3 AppStateManager
 * 
 * @author rpax
 *
 */
@SuppressWarnings("unchecked")
public class VertxAppSystemManagerImpl implements AppSystemManager {

	private static final Logger log = LoggerFactory.getLogger(VertxAppSystemManagerImpl.class);
	/**
	 * List holding the attached app states that are pending initialization.
	 * Once initialized they will be added to the running app states.
	 */
	private final SafeArrayList<MassisSystem> initializing = new SafeArrayList<>(
			MassisSystem.class);

	/**
	 * Holds the active states once they are initialized.
	 */
	private final SafeArrayList<MassisSystem> systems = new SafeArrayList<>(MassisSystem.class);

	/**
	 * List holding the detached app states that are pending cleanup.
	 */
	private final SafeArrayList<MassisSystem> terminating = new SafeArrayList<>(MassisSystem.class);

	private AssetManager assetManager = Materials.newAssetManager();

	private float tpf;

	private float time;

	private Node rootNode;

	private boolean initialized;

	private AtomicBoolean isShutdown;
	private MultiCameraApp application;

	private Map<MassisSystem, Node> systemNodes;
	private boolean windowed;
	private Vertx vertx;

	public VertxAppSystemManagerImpl(Vertx vertx, boolean windowed, Collection<MassisSystem> states)
	{
		this.vertx = vertx;
		this.windowed = windowed;
		this.initialized = false;
		this.isShutdown = new AtomicBoolean(false);
		this.systemNodes = new HashMap<>();
		this.rootNode = new Node("[App System Manager root node]");
		checkDuplicates(states);
		SystemsTopologicalSorter.sort(states)
				.stream()
				.map(MassisSystem.class::cast)
				.forEach(initializing::add);
	}

	@Override
	public void createCamera(int width, int height, Handler<AsyncResult<ServerCamera>> handler)
	{

		this.application.createCamera(width, height, ar -> {
			if (ar.failed())
			{
				handler.handle(Future.failedFuture(ar.cause()));
			} else
			{
				enqueue(() -> {
					try
					{
						InternalServerCamera cam = ar.result();
						cam.attachScene(this.rootNode);
						handler.handle(Future.succeededFuture(cam));
					} catch (Exception e)
					{
						log.error("Error when attaching camera", e);
						handler.handle(Future.failedFuture(e));
					}

				});
			}
		});

	}

	@Override
	public void releaseCamera(ServerCamera cam, Handler<AsyncResult<Void>> handler)
	{
		this.application.removeCamera(cam, handler);
	}

	protected MassisSystem[] getInitializing()
	{
		synchronized (systems)
		{
			return initializing.getArray();
		}
	}

	protected MassisSystem[] getTerminating()
	{
		synchronized (systems)
		{
			return terminating.getArray();
		}
	}

	protected MassisSystem[] getStates()
	{
		synchronized (systems)
		{
			return systems.getArray();
		}
	}

	protected void terminatePending()
	{
		MassisSystem[] array = getTerminating();
		if (array.length == 0)
			return;

		for (MassisSystem state : array)
		{
			state.cleanup();
		}
		synchronized (systems)
		{
			// Remove just the states that were terminated...
			// which might now be a subset of the total terminating
			// list.
			terminating.removeAll(Arrays.asList(array));
		}
	}

	@Override
	public void initialize(Handler<AsyncResult<Void>> handler)
	{
		if (this.initialized)
		{
			throw new IllegalStateException("Already initialized");
		} else
		{
			this.initialized = true;
			// windowed??
			MultiCameraApp.launch(windowed, ar -> {
				if (ar.failed())
				{
					vertx.runOnContext(_void -> handler.handle(Future.failedFuture(ar.cause())));
				} else
				{

					this.application = ar.result();
					this.application.enqueue(() -> {
						try
						{
							this.initializePending();
							vertx.runOnContext(_void -> handler.handle(Future.succeededFuture()));
						} catch (Exception e)
						{
							vertx.runOnContext(_void -> handler.handle(Future.failedFuture(e)));
						}
					});
				}
			});
		}
	}

	@Override
	public void start(Handler<AsyncResult<Void>> handler)
	{
		// y aqui comenzamos el loop de update
		this.application.getStateManager().attach(new AbstractAppState() {
			@Override
			public void update(float tpf)
			{
				VertxAppSystemManagerImpl.this.update(tpf);
			}

			@Override
			public void initialize(AppStateManager stateManager, Application app)
			{
				vertx.runOnContext(_void -> handler.handle(Future.succeededFuture()));
			}

			@Override
			public void cleanup()
			{
				MassisSystem[] array = getStates();
				for (MassisSystem state : array)
				{
					state.cleanup();
				}
			}
		});
	}

	protected void initializePending()
	{
		MassisSystem[] array = getInitializing();
		if (array.length == 0)
			return;

		synchronized (systems)
		{
			// Move the states that will be initialized
			// into the active array. In all but one case the
			// order doesn't matter but if we do this here then
			// a state can detach itself in initialize(). If we
			// did it after then it couldn't.
			List<MassisSystem> transfer = Arrays.asList(array);
			systems.addAll(transfer);
			initializing.removeAll(transfer);
		}
		for (MassisSystem system : array)
		{
			Node systemNode = new Node("_DEBUG_NODE_" + system.getClass().getName() + "_UID_"
					+ UUID.randomUUID().toString());
			this.rootNode.attachChild(systemNode);
			this.systemNodes.put(system, systemNode);
			system.initialize(this);
		}
	}

	/**
	 * Calls update for attached states, do not call directly.
	 * 
	 * @param tpf
	 *            Time per frame.
	 */
	public void update(float tpf)
	{
		try
		{
			if (this.isShutdown.get())
			{
				log.warn("Manager is shutted down");
			}

			this.tpf = tpf;
			this.time += tpf;
			this.rootNode.updateGeometricState();
			// Cleanup any states pending
			terminatePending();

			// Initialize any states pending
			initializePending();

			// Update enabled states
			MassisSystem[] array = getStates();

			// Logical update
			for (MassisSystem state : array)
			{
				if (state.isEnabled())
				{
					state.update(tpf);
				}
			}

			for (MassisSystem system : array)
			{
				if (system.isEnabled())
				{
					system.graphicalUpdate(systemNodes.get(system), tpf);
				}
				system.runPendingTasks(systemNodes.get(system));
			}
			this.rootNode.updateLogicalState(tpf);
			this.rootNode.updateGeometricState();

		} catch (Exception e)
		{
			log.error("Error in update loop", e);
		}

	}

	private static void checkDuplicates(Collection<MassisSystem> states)
	{
		Set<Class<?>> types = new HashSet<>();
		for (MassisSystem s : states)
		{
			if (types.contains(s.getClass()))
			{
				throw new IllegalStateException("Duplicated state :" + s.getClass().getName());
			} else
			{
				types.add(s.getClass());
			}
		}
	}

	public Node getRootNode()
	{
		return this.rootNode;
	}

	@Override
	public float tpf()
	{
		return this.tpf;
	}

	@Override
	public float time()
	{
		return this.time;
	}

	@Override
	/**
	 * Returns the first state that is an instance of subclass of the specified
	 * class.
	 * 
	 * @param <T>
	 * @param stateClass
	 * @return First attached state that is an instance of stateClass
	 */
	public <T extends MassisSystem> T getSystem(Class<T> stateClass)
	{
		synchronized (systems)
		{
			MassisSystem[] array = getStates();
			for (MassisSystem state : array)
			{
				if (stateClass.isAssignableFrom(state.getClass()))
				{
					return (T) state;
				}
			}

			// This may be more trouble than its worth but I think
			// it's necessary for proper decoupling of states and provides
			// similar behavior to before where a state could be looked
			// up even if it wasn't initialized. -pspeed
			array = getInitializing();
			for (MassisSystem state : array)
			{
				if (stateClass.isAssignableFrom(state.getClass()))
				{
					return (T) state;
				}
			}
		}
		return null;
	}

	@Override
	public AssetManager getDefaultAssetManager()
	{
		return this.assetManager;
	}

	@Override
	public void shutdown()
	{
		if (this.isShutdown.getAndSet(true))
		{
			return;
		}
		this.application.stop(false);
	}

	@Override
	public List<ServerCamera> getCameras()
	{
		return this.application.getCameras();
	}

	@Override
	public void enqueue(Runnable r)
	{
		this.application.enqueue(r);
	}

	@Override
	public Set<Class<? extends MassisSystem>> getRunningSystems()
	{
		return Arrays.stream(getStates()).map(s -> s.getClass()).collect(Collectors.toSet());
	}

	@Override
	public void setSystemEnabled(
			Class<? extends MassisSystem> systemType,
			boolean enabled,
			Handler<AsyncResult<Boolean>> handler)
	{
		enqueue(() -> {
			MassisSystem system = getSystem(systemType);
			if (system == null)
			{
				vertx.runOnContext(
						_void -> handler.handle(Future.failedFuture("System not present")));
			} else
			{
				boolean changed = enabled != system.isEnabled();
				system.setEnabled(enabled);
				vertx.runOnContext(_void -> handler.handle(Future.succeededFuture(changed)));
			}
		});
	}
}
