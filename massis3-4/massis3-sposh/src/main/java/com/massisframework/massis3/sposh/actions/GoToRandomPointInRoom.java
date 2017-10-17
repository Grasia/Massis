package com.massisframework.massis3.sposh.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.math.Vector3f;
import com.massisframework.massis3.sposh.ContextKey;
import com.massisframework.massis3.sposh.Massis3Agent;
import com.massisframework.massis3.sposh.Massis3Context;
import com.massisframework.massis3.sposh.executor.MassisAction;
import com.massisframework.massis3.sposh.util.GeometryUtils;

import cz.cuni.amis.pogamut.sposh.executor.ActionResult;
import cz.cuni.amis.pogamut.sposh.executor.Param;
import cz.cuni.amis.pogamut.sposh.executor.PrimitiveInfo;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Action Echo for Yaposh.
 *
 * @author rpax
 * @param <CONTEXT>
 *            Context class of the action. It's an shared object used by all
 *            primitives. it is used as a shared memory and for interaction with
 *            the environment.
 */
@PrimitiveInfo(
		name = "Set Target room",
		description = "Specifies a room as target. The concrete point of the room is chosen randomly")
public class GoToRandomPointInRoom extends MassisAction {

	private static final Logger log = LoggerFactory.getLogger(GoToRandomPointInRoom.class);
	private String roomName;

	/**
	 * Constructor of the action, used during automatic instantiation.
	 */
	public GoToRandomPointInRoom(Massis3Context<Massis3Agent> ctx)
	{
		super(ctx);
	}

	/**
	 * Method responsible for initialization of the action. The method can be
	 * passed parameters from the plan. Add all desired plan parameters as
	 * method parameters, e.g. <tt>public void init({@literal @}Param("$speed")
	 * Integer runningSpeed)</tt>.
	 */
	public void init(@Param("$roomName") String roomName)
	{
		this.roomName = roomName;
	}

	@Override
	protected void fistRun()
	{
		// this.getCtx().setContextValue(ContextKey.MOVEMENT_TARGET, new
		// Vector3f(x, y, z));
		this.agent().findEntitiesByName(this.roomName, res -> {
			if (res.failed())
			{
				log.error("Request failed", res.cause());
				setResult(ActionResult.FAILED);
				return;
			}
			JsonArray roomIds = res.result();
			if (roomIds.isEmpty())
			{
				log.warn("No room with name {} found", this.roomName);
				setResult(ActionResult.FAILED);
				return;
			}
			if (roomIds.size() > 1)
			{
				log.warn("There are {} rooms with the same name. Choosing first", roomIds.size());
			}
			long id = roomIds.getLong(0);
			this.agent().getRoomInfo(id, r2 -> {
				if (r2.failed())
				{
					log.error("Error when retrieving room info", r2.cause());
					setResult(ActionResult.FAILED);
					return;
				}
				JsonObject roomInfo = r2.result();
				JsonArray roomPoints = roomInfo.getJsonArray("points");
				Vector3f randomPoint = GeometryUtils.randomPointInPolygon(roomPoints);
				this.getCtx().setContextValue(ContextKey.MOVEMENT_TARGET, randomPoint);
				setResult(ActionResult.FINISHED);
				return;
			});

		});

	}

	@Override
	public void logic()
	{

	}

}
