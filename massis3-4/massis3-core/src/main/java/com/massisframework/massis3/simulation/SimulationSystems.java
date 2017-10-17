package com.massisframework.massis3.simulation;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.reflections.Reflections;

import com.jme3.app.DebugKeysAppState;
import com.jme3.app.FlyCamAppState;
import com.jme3.app.StatsAppState;
import com.jme3.app.state.AppState;
import com.massisframework.massis3.commons.app.server.MassisSystem;
import com.massisframework.massis3.commons.app.system.BackgroundTasksSystem;
import com.massisframework.massis3.commons.app.system.DebugShapesSystem;
import com.massisframework.massis3.commons.app.system.FlyCamFocusAppState;
import com.massisframework.massis3.core.systems.camera.imagewriter.ImageRenderAppState;
import com.massisframework.massis3.core.systems.control.human.HumanControlSystem;
import com.massisframework.massis3.core.systems.debug.NavmeshVisualizerSystem;
import com.massisframework.massis3.core.systems.debug.PathInfoDebugAppSystem;
import com.massisframework.massis3.core.systems.debug.ShowExtentsBoxAppSystem;
import com.massisframework.massis3.core.systems.debug.ShowIdsDebugSystem;
import com.massisframework.massis3.core.systems.debug.ShowRoomAreasAppSystem;
import com.massisframework.massis3.core.systems.debug.ShowUniformGridGraphAppSystem;
import com.massisframework.massis3.core.systems.debug.Text3DManagerSystem;
import com.massisframework.massis3.core.systems.engine.CountDownDestroySystem;
import com.massisframework.massis3.core.systems.engine.SceneWireUpAppSystem;
import com.massisframework.massis3.core.systems.engine.VelocityRotationSystem;
import com.massisframework.massis3.core.systems.engine.navigation.CellLocationSystem;
import com.massisframework.massis3.core.systems.engine.navigation.CellPathDirectionSystem;
import com.massisframework.massis3.core.systems.engine.navigation.ClearPathFollowSystem;
import com.massisframework.massis3.core.systems.engine.navigation.CollisionFreeVelocityGeneratorSystem;
import com.massisframework.massis3.core.systems.engine.navigation.NavmeshHolderSystem;
import com.massisframework.massis3.core.systems.engine.navigation.PathGeneratorSystem;
import com.massisframework.massis3.core.systems.engine.navigation.UniformGridSystem;
import com.massisframework.massis3.core.systems.engine.navigation.rvo2.RVO2System;
import com.massisframework.massis3.core.systems.engine.physics.SimpleGravitySystem;
import com.massisframework.massis3.core.systems.gui.JavaFXSystem;
import com.massisframework.massis3.core.systems.scene.AnimationSystem;
import com.massisframework.massis3.core.systems.scene.ModelPositionSystem;
import com.massisframework.massis3.core.systems.scene.SceneGraphSystem;

public class SimulationSystems {

	public static Collection<Class<? extends MassisSystem>> debugStates()
	{
		return Arrays.asList(
				ShowRoomAreasAppSystem.class,
				ShowExtentsBoxAppSystem.class,
				ShowUniformGridGraphAppSystem.class,
				ShowIdsDebugSystem.class,
				Text3DManagerSystem.class,
				DebugShapesSystem.class,
				NavmeshVisualizerSystem.class,
				PathInfoDebugAppSystem.class);
	}

	public static Collection<Class<? extends AppState>> jmeGUIStates()
	{
		return Arrays.asList(
				JavaFXSystem.class,
				// TODO this is not GUI
				ImageRenderAppState.class,
				StatsAppState.class,
				FlyCamAppState.class,
				FlyCamFocusAppState.class,
				DebugKeysAppState.class);
	}

	public static Collection<Class<? extends MassisSystem>> netStates()
	{
		return Arrays.asList(
		// VertxAppState.class
		// TelnetTerminalVerticle.class

		);
	}

	public static Collection<Class<? extends MassisSystem>> startupStates()
	{
		return Arrays.asList(
				BackgroundTasksSystem.class,
				SceneWireUpAppSystem.class,
				CountDownDestroySystem.class);
	}

	public static Collection<Class<? extends MassisSystem>> navmeshStates()
	{
		return Arrays.asList(
				UniformGridSystem.class,
				NavmeshHolderSystem.class,
				CellLocationSystem.class);
	}

	public static Collection<Class<? extends MassisSystem>> sceneGraphStates()
	{
		return Arrays.asList(AnimationSystem.class, ModelPositionSystem.class,
				SceneGraphSystem.class);
	}

	public static Collection<Class<? extends MassisSystem>> humanManagementStates()
	{
		return Arrays.asList(
				HumanControlSystem.class,
				VelocityRotationSystem.class);
	}

	public static Collection<Class<? extends MassisSystem>> physicsStates()
	{
		return Arrays.asList(SimpleGravitySystem.class);
	}

	public static Collection<Class<? extends MassisSystem>> pathFollowingAppStates()
	{
		return Arrays.asList(
				// SteeringDirectionSystem.class,
				CellPathDirectionSystem.class,
				RVO2System.class,
				CollisionFreeVelocityGeneratorSystem.class,
				ClearPathFollowSystem.class,
				PathGeneratorSystem.class);
	}

	public static List<Class<? extends MassisSystem>> defaultSystems()
	{
		return Arrays.asList(
				debugStates(),
				netStates(),
				/* guiStates(), */
				startupStates(),
				navmeshStates(),
				physicsStates(),
				sceneGraphStates(),
				humanManagementStates(),
				pathFollowingAppStates())
				.stream()
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}

	@SafeVarargs
	public static List<MassisSystem> instantiate(
			Class<? extends MassisSystem> s1,
			Class<? extends MassisSystem>... stateTypes)
	{
		return instantiate(
				Stream.concat(Stream.of(s1), Arrays.stream(stateTypes))
						.collect(Collectors.toList()));
	}

	@SafeVarargs
	public static List<MassisSystem> instantiate(
			Collection<Class<? extends MassisSystem>>... stateTypes)
	{
		return instantiate(
				Arrays.stream(stateTypes)
						.flatMap(Collection::stream)
						.collect(Collectors.toList()));
	}

	public static List<MassisSystem> instantiate(
			Collection<Class<? extends MassisSystem>> stateTypes)
	{
		// AppStateTopologicalSorter .sortByClass(stateTypes)
		return stateTypes
				.stream()
				.distinct()
				.map(s -> instantiate(s))
				.collect(Collectors.toList());
	}

	public static MassisSystem instantiate(Class<? extends MassisSystem> type)
	{
		try
		{
			return type.newInstance();
		} catch (InstantiationException | IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
	}

	private static Set<Class<? extends MassisSystem>> CLASSPATH_APPSTATES;

	public static synchronized Set<Class<? extends MassisSystem>> classpathAppStates()
	{
		if (CLASSPATH_APPSTATES == null)
		{
			CLASSPATH_APPSTATES = new Reflections("com.massisframework")
					.getSubTypesOf(MassisSystem.class);
		}
		return CLASSPATH_APPSTATES;
	}
}
