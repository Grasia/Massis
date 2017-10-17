package com.massisframework.massis3.simulation.server.eventbus.services;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.massisframework.massis3.commons.app.server.AppSystemManager;
import com.massisframework.massis3.commons.app.server.ServerCamera;
import com.massisframework.massis3.core.components.NameComponent;
import com.massisframework.massis3.core.components.Position;
import com.massisframework.massis3.core.components.RoomComponent;
import com.massisframework.massis3.core.systems.required.EntityDataSystem;
import com.massisframework.massis3.core.systems.required.SceneLoaderSystem;
import com.massisframework.massis3.services.dataobjects.JsonPoint;
import com.massisframework.massis3.services.dataobjects.JsonQuaternion;
import com.massisframework.massis3.services.eventbus.sim.EnvironmentService;
import com.massisframework.massis3.simulation.ecs.EntityComponentAccessor;
import com.simsilica.es.EntityId;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageProducer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class EnvironmentServiceImpl extends AbstractVerticle implements EnvironmentService {

	private static final Logger log = LoggerFactory.getLogger(EnvironmentServiceImpl.class);
	private EntityComponentAccessor eqs;
	private Supplier<Float> timeProvider;
	private String sceneName;
	private MessageProducer<Float> timePublisher;
	private ConcurrentHashMap<String, String> friendlyCameraIds;
	private AtomicLong friendlyCameraCounter;
	private AppSystemManager systemsManager;
	private int camWidth;
	private int camHeight;
	private String camerasMapKey;

	public EnvironmentServiceImpl(AppSystemManager systemsManager)

	{
		this.friendlyCameraCounter = new AtomicLong();
		this.friendlyCameraIds = new ConcurrentHashMap<>();
		this.systemsManager = systemsManager;
		EntityDataSystem edAppState = systemsManager.getSystem(EntityDataSystem.class);
		this.timeProvider = edAppState::time;
		this.eqs = edAppState.createAbsoluteAccessor();
		this.sceneName = systemsManager.getSystem(SceneLoaderSystem.class).getSimulationSceneFile();
		// add cameras if they exist.

	}

	@Override
	public void start() throws Exception
	{

		if (log.isInfoEnabled())
		{
			log.info("Initializing environment service.");
		}

		this.camerasMapKey = UUID.randomUUID().toString();
		// TODO hardcoded
		this.camWidth = 1024;
		this.camHeight = 768;

		this.timePublisher = this.vertx.eventBus().publisher(UUID.randomUUID().toString());
		vertx.setPeriodic(250, tId -> this.timePublisher.send(timeProvider.get()));
		this.systemsManager.getCameras().forEach(cam -> this.attachCameraToVertxLoop(cam));

	}

	@Override
	public void roomIds(Handler<AsyncResult<JsonArray>> resultHandler)
	{
		JsonArray result = new JsonArray();
		getRoomIds().stream().map(entityId -> entityId.getId()).forEach(result::add);
		resultHandler.handle(Future.succeededFuture(result));
	}

	@Override
	public void allRoomsInfo(Handler<AsyncResult<JsonArray>> resultHandler)
	{
		JsonArray res = new JsonArray();
		getRoomIds().stream().map(rId -> toJsonRoom(rId)).forEach(res::add);
		resultHandler.handle(Future.succeededFuture(res));
	}

	@Override
	public void roomInfo(long id, Handler<AsyncResult<JsonObject>> resultHandler)
	{
		EntityId eid = new EntityId(id);
		if (this.eqs.get(eid, RoomComponent.class) == null)
		{
			resultHandler.handle(Future.failedFuture("The id provided is not a room"));
			return;
		} else
		{
			resultHandler.handle(Future.succeededFuture(toJsonRoom(eid)));
		}
	}

	private Set<EntityId> getRoomIds()
	{
		return this.eqs.findEntities(null, RoomComponent.class, NameComponent.class);
	}

	private JsonObject toJsonRoom(EntityId entityId)
	{
		JsonObject obj = eqs.get(entityId, RoomComponent.class).toJson();
		String name = eqs.get(entityId, NameComponent.class).getName();
		obj.put("name", name);
		obj.put("entityId", entityId.getId());
		// TODO remove non-necessary info
		obj.remove("__type");
		obj.remove("unitVector");
		return obj;
	}

	@Override
	public void entitiesNamed(String name, Handler<AsyncResult<JsonArray>> resultHandler)
	{
		JsonArray array = new JsonArray();
		this.eqs.findEntities(null, NameComponent.class).stream()
				.filter(eid -> name.equals(eqs.get(eid, NameComponent.class).getName()))
				.map(EntityId::getId).forEach(array::add);
		resultHandler.handle(Future.succeededFuture(array));
	}

	@Override
	public void entityLocation(long id, Handler<AsyncResult<JsonPoint>> resultHandler)
	{
		EntityId eid = new EntityId(id);
		Position pos = this.eqs.get(eid, Position.class);
		if (pos == null)
		{
			resultHandler.handle(Future.failedFuture("Entity does not have a position"));
			return;
		}
		Vector3f loc = pos.get();
		resultHandler.handle(Future.succeededFuture(new JsonPoint(loc.x, loc.y, loc.z)));
	}

	@Override
	public void time(Handler<AsyncResult<Float>> resultHandler)
	{
		resultHandler.handle(Future.succeededFuture(this.timeProvider.get()));
	}

	@Override
	public void sceneName(Handler<AsyncResult<String>> resultHandler)
	{
		resultHandler.handle(Future.succeededFuture(sceneName));
	}

	@Override
	public void timeStreamAddress(Handler<AsyncResult<String>> resultHandler)
	{
		resultHandler.handle(Future.succeededFuture(this.timePublisher.address()));
	}

	@Override
	public void stop() throws Exception
	{

		if (log.isInfoEnabled())
		{
			log.info("Undeploying verticle");
		}
	}

	@Override
	public void addCamera(Handler<AsyncResult<String>> resultHandler)
	{

		if (log.isInfoEnabled())
		{
			log.info("Adding camera");
		}

		this.systemsManager.createCamera(this.camWidth, this.camHeight, ar -> {
			if (ar.failed())
			{
				resultHandler.handle(Future.failedFuture(ar.cause()));
			} else
			{
				ServerCamera cam = ar.result();
				String friendlyId = attachCameraToVertxLoop(cam);
				resultHandler.handle(Future.succeededFuture(friendlyId));
			}
		});

	}

	private String attachCameraToVertxLoop(ServerCamera cam)
	{

		if (log.isInfoEnabled())
		{
			log.info("Attaching camera to vert.x loop");
		}

		String friendlyId = String.valueOf(this.friendlyCameraCounter.getAndIncrement());
		this.friendlyCameraIds.put(friendlyId, cam.getId());
		vertx.setPeriodic(100, tId -> {
			if (cam.isReleased())
			{
				vertx.cancelTimer(tId);
				removeCameraData(friendlyId);
			} else
			{
				putCameraData(friendlyId, cam.getImage());
			}
		});
		return friendlyId;
	}

	private void putCameraData(String friendlyId, BufferedImage image)
	{
		if (image == null)
		{
			return;
		}
		// ByteArrayOutputStream baos = getCameraData_Baos_TL.get();
		Buffer buff = Buffer.buffer(image.getHeight() * image.getWidth());
		try
		{
			ImageIO.write(image, "jpg", VertxBufferOutputStream.wrap(buff));
			vertx.sharedData().getLocalMap(camerasMapKey).put(friendlyId, buff);
			vertx.eventBus().publish(camerasMapKey + friendlyId, buff);
		} catch (IOException e)
		{
			log.error("Unexpected error", e);
		}

	}

	private void removeCameraData(String friendlyId)
	{
		String uuid = this.friendlyCameraIds.remove(friendlyId);
		if (uuid == null)
		{
			return;
		}
		vertx.sharedData().getLocalMap(camerasMapKey).remove(friendlyId);
	}

	@Override
	public void setCameraLocation(String camId, JsonPoint location,
			Handler<AsyncResult<Void>> resultHandler)
	{
		try
		{
			ServerCamera cam = getCamera(camId);
			if (cam == null)
			{
				handleCameraNotPresent(camId, resultHandler);
				return;
			}
			cam.setLocation(new Vector3f(
					location.getX(),
					location.getY(),
					location.getZ()));
			resultHandler.handle(Future.succeededFuture());
		} catch (Exception e)
		{
			log.error("Error when setting camera location", e);
			resultHandler.handle(Future.failedFuture(e));
		}

	}

	@Override
	public void setCameraRotation(String camId, JsonQuaternion rotation,
			Handler<AsyncResult<Void>> resultHandler)
	{
		try
		{
			ServerCamera cam = getCamera(camId);
			if (cam == null)
			{
				handleCameraNotPresent(camId, resultHandler);
				return;
			}
			cam.setRotation(new Quaternion(
					rotation.getX(),
					rotation.getY(),
					rotation.getZ(),
					rotation.getW()));
			resultHandler.handle(Future.succeededFuture());

		} catch (Exception e)
		{
			log.error("Error when setting camera location", e);
			resultHandler.handle(Future.failedFuture(e));
		}

	}

	private ServerCamera getCamera(String friendlyId)
	{
		String camId = this.friendlyCameraIds.getOrDefault(friendlyId, friendlyId);
		if (camId == null)
		{
			return null;
		}
		ServerCamera cam = this.systemsManager.getCameras().stream()
				.filter(c -> c.getId().equals(camId)).findAny()
				.orElse(null);
		return cam;
	}

	@Override
	public void cameraIds(Handler<AsyncResult<JsonArray>> resultHandler)
	{
		JsonArray res = new JsonArray();
		this.systemsManager.getCameras().stream().map(c -> c.getId()).forEach(res::add);
		resultHandler.handle(Future.succeededFuture(res));
	}

	@Override
	public void setCameraRotationWithAngles(String camId, float xAngle, float yAngle, float zAngle,
			Handler<AsyncResult<Void>> resultHandler)
	{

		try
		{
			ServerCamera cam = getCamera(camId);
			if (cam == null)
			{
				handleCameraNotPresent(camId, resultHandler);
				return;
			}
			cam.setRotation(new Quaternion().fromAngles(xAngle, yAngle, zAngle));
			resultHandler.handle(Future.succeededFuture());

		} catch (Exception e)
		{
			log.error("Error when setting camera location", e);
			resultHandler.handle(Future.failedFuture(e));
		}

	}

	@Override
	public void cameraMapKeyValue(String camId, Handler<AsyncResult<JsonArray>> resultHandler)
	{
		ServerCamera cam = getCamera(camId);
		if (cam == null)
		{
			handleCameraNotPresent(camId, resultHandler);
			return;
		}
		resultHandler
				.handle(Future.succeededFuture(new JsonArray().add(this.camerasMapKey).add(camId)));
	}

	private <T> void handleCameraNotPresent(String camId, Handler<AsyncResult<T>> handler)
	{
		handler.handle(Future.failedFuture(
				"Camera with id [" + camId + "] is not present. " + this.friendlyCameraIds));
	}

	@Override
	public void cameraDataStreamEventBusAddress(String camId, Handler<AsyncResult<String>> handler)
	{
		ServerCamera cam = getCamera(camId);
		if (cam == null)
		{
			handleCameraNotPresent(camId, handler);
			return;
		}
		handler.handle(Future.succeededFuture(camId + this.friendlyCameraIds.get(camId)));
	}
}
