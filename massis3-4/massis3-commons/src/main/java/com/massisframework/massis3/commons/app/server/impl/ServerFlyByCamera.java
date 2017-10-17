package com.massisframework.massis3.commons.app.server.impl;

import com.jme3.collision.MotionAllowedListener;
import com.jme3.input.FlyByCamera;
import com.jme3.input.InputManager;
import com.jme3.input.Joystick;
import com.jme3.input.JoystickAxis;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.massisframework.massis3.commons.app.server.InternalServerCamera;

public class ServerFlyByCamera implements AnalogListener, ActionListener {

	protected InternalServerCamera cam;
	protected Vector3f initialUpVec;
	protected float rotationSpeed = 1f;
	protected float moveSpeed = 3f;
	protected float zoomSpeed = 1f;
	protected MotionAllowedListener motionAllowed = null;
	protected boolean enabled = true;
	protected boolean dragToRotate = true;
	protected boolean canRotate = false;
	protected boolean invertY = false;
	protected InputManager inputManager;

	/**
	 * Creates a new FlyByCamera to control the given Camera object.
	 * 
	 * @param cam
	 */
	public ServerFlyByCamera(InternalServerCamera cam)
	{
		this.cam = cam;
		initialUpVec = cam.getUp().clone();
	}

	/**
	 * Sets the up vector that should be used for the camera.
	 * 
	 * @param upVec
	 */
	public void setUpVector(Vector3f upVec)
	{
		initialUpVec.set(upVec);
	}

	public void setMotionAllowedListener(MotionAllowedListener listener)
	{
		this.motionAllowed = listener;
	}

	/**
	 * Sets the move speed. The speed is given in world units per second.
	 * 
	 * @param moveSpeed
	 */
	public void setMoveSpeed(float moveSpeed)
	{
		this.moveSpeed = moveSpeed;
	}

	/**
	 * Gets the move speed. The speed is given in world units per second.
	 * 
	 * @return moveSpeed
	 */
	public float getMoveSpeed()
	{
		return moveSpeed;
	}

	/**
	 * Sets the rotation speed.
	 * 
	 * @param rotationSpeed
	 */
	public void setRotationSpeed(float rotationSpeed)
	{
		this.rotationSpeed = rotationSpeed;
	}

	/**
	 * Gets the move speed. The speed is given in world units per second.
	 * 
	 * @return rotationSpeed
	 */
	public float getRotationSpeed()
	{
		return rotationSpeed;
	}

	/**
	 * Sets the zoom speed.
	 * 
	 * @param zoomSpeed
	 */
	public void setZoomSpeed(float zoomSpeed)
	{
		this.zoomSpeed = zoomSpeed;
	}

	/**
	 * Gets the zoom speed. The speed is a multiplier to increase/decrease the
	 * zoom rate.
	 * 
	 * @return zoomSpeed
	 */
	public float getZoomSpeed()
	{
		return zoomSpeed;
	}

	/**
	 * @param enable
	 *            If false, the camera will ignore input.
	 */
	public void setEnabled(boolean enable)
	{
		if (enabled && !enable)
		{
			if (inputManager != null && (!dragToRotate || (dragToRotate && canRotate)))
			{
				inputManager.setCursorVisible(true);
			}
		}
		enabled = enable;
	}

	/**
	 * @return If enabled
	 * @see FlyByCamera#setEnabled(boolean)
	 */
	public boolean isEnabled()
	{
		return enabled;
	}

	/**
	 * @return If drag to rotate feature is enabled.
	 *
	 * @see FlyByCamera#setDragToRotate(boolean)
	 */
	public boolean isDragToRotate()
	{
		return dragToRotate;
	}

	/**
	 * Set if drag to rotate mode is enabled.
	 * 
	 * When true, the user must hold the mouse button and drag over the screen
	 * to rotate the camera, and the cursor is visible until dragged. Otherwise,
	 * the cursor is invisible at all times and holding the mouse button is not
	 * needed to rotate the camera. This feature is disabled by default.
	 * 
	 * @param dragToRotate
	 *            True if drag to rotate mode is enabled.
	 */
	public void setDragToRotate(boolean dragToRotate)
	{
		this.dragToRotate = dragToRotate;
		if (inputManager != null)
		{
			inputManager.setCursorVisible(dragToRotate);
		}
	}

	/**
	 * Registers the FlyByCamera to receive input events from the provided
	 * Dispatcher.
	 * 
	 * @param inputManager
	 */
	public void registerWithInput(InputManager inputManager)
	{
		this.inputManager = inputManager;

		// both mouse and button - rotation of cam
		inputManager.addMapping(FLYCAM_LEFT,
				new MouseAxisTrigger(MouseInput.AXIS_X, true),
				new KeyTrigger(KeyInput.KEY_LEFT));

		inputManager.addMapping(FLYCAM_RIGHT,
				new MouseAxisTrigger(MouseInput.AXIS_X, false),
				new KeyTrigger(KeyInput.KEY_RIGHT));

		inputManager.addMapping(FLYCAM_UP,
				new MouseAxisTrigger(MouseInput.AXIS_Y, false),
				new KeyTrigger(KeyInput.KEY_UP));

		inputManager.addMapping(FLYCAM_DOWN,
				new MouseAxisTrigger(MouseInput.AXIS_Y, true),
				new KeyTrigger(KeyInput.KEY_DOWN));

		// mouse only - zoom in/out with wheel, and rotate drag
		inputManager.addMapping(FLYCAM_ZOOMIN,
				new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
		inputManager.addMapping(FLYCAM_ZOOMOUT,
				new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
		inputManager.addMapping(FLYCAM_ROTATEDRAG,
				new MouseButtonTrigger(MouseInput.BUTTON_LEFT));

		// keyboard only WASD for movement and WZ for rise/lower height
		inputManager.addMapping(FLYCAM_STRAFELEFT, new KeyTrigger(KeyInput.KEY_A));
		inputManager.addMapping(FLYCAM_STRAFERIGHT, new KeyTrigger(KeyInput.KEY_D));
		inputManager.addMapping(FLYCAM_FORWARD, new KeyTrigger(KeyInput.KEY_W));
		inputManager.addMapping(FLYCAM_BACKWARD, new KeyTrigger(KeyInput.KEY_S));
		inputManager.addMapping(FLYCAM_RISE, new KeyTrigger(KeyInput.KEY_Q));
		inputManager.addMapping(FLYCAM_LOWER, new KeyTrigger(KeyInput.KEY_Z));

		inputManager.addListener(this, mappings);
		inputManager.setCursorVisible(dragToRotate || !isEnabled());

		Joystick[] joysticks = inputManager.getJoysticks();
		if (joysticks != null && joysticks.length > 0)
		{
			for (Joystick j : joysticks)
			{
				mapJoystick(j);
			}
		}
	}

	protected void mapJoystick(Joystick joystick)
	{

		// Map it differently if there are Z axis
		if (joystick.getAxis(JoystickAxis.Z_ROTATION) != null
				&& joystick.getAxis(JoystickAxis.Z_AXIS) != null)
		{

			// Make the left stick move
			joystick.getXAxis().assignAxis(FLYCAM_STRAFERIGHT,
					FLYCAM_STRAFELEFT);
			joystick.getYAxis().assignAxis(FLYCAM_BACKWARD, FLYCAM_FORWARD);

			// And the right stick control the camera
			joystick.getAxis(JoystickAxis.Z_ROTATION).assignAxis(FLYCAM_DOWN,
					FLYCAM_UP);
			joystick.getAxis(JoystickAxis.Z_AXIS).assignAxis(FLYCAM_RIGHT,
					FLYCAM_LEFT);

			// And let the dpad be up and down
			joystick.getPovYAxis().assignAxis(FLYCAM_RISE, FLYCAM_LOWER);

			if (joystick.getButton("Button 8") != null)
			{
				// Let the stanard select button be the y invert toggle
				joystick.getButton("Button 8").assignButton(FLYCAM_INVERTY);
			}

		} else
		{
			joystick.getPovXAxis().assignAxis(FLYCAM_STRAFERIGHT,
					FLYCAM_STRAFELEFT);
			joystick.getPovYAxis().assignAxis(FLYCAM_FORWARD,
					FLYCAM_BACKWARD);
			joystick.getXAxis().assignAxis(FLYCAM_RIGHT, FLYCAM_LEFT);
			joystick.getYAxis().assignAxis(FLYCAM_DOWN, FLYCAM_UP);
		}
	}

	/**
	 * Unregisters the FlyByCamera from the event Dispatcher.
	 */
	public void unregisterInput()
	{

		if (inputManager == null)
		{
			return;
		}

		for (String s : mappings)
		{
			if (inputManager.hasMapping(s))
			{
				inputManager.deleteMapping(s);
			}
		}

		inputManager.removeListener(this);
		inputManager.setCursorVisible(!dragToRotate);

		Joystick[] joysticks = inputManager.getJoysticks();
		if (joysticks != null && joysticks.length > 0)
		{
			Joystick joystick = joysticks[0];

			// No way to unassing axis
		}
	}

	protected void rotateCamera(float value, Vector3f axis)
	{
		if (dragToRotate)
		{
			if (canRotate)
			{
				// value = -value;
			} else
			{
				return;
			}
		}

		Matrix3f mat = new Matrix3f();
		mat.fromAngleNormalAxis(rotationSpeed * value, axis);

		Vector3f up = cam.getUp();
		Vector3f left = cam.getLeft();
		Vector3f dir = cam.getDirection();

		mat.mult(up, up);
		mat.mult(left, left);
		mat.mult(dir, dir);

		Quaternion q = new Quaternion();
		q.fromAxes(left, up, dir);
		q.normalizeLocal();

		cam.setAxes(q);
	}

	protected void zoomCamera(float value)
	{
		// derive fovY value
		float h = cam.getFrustumTop();
		float w = cam.getFrustumRight();
		float aspect = w / h;

		float near = cam.getFrustumNear();

		float fovY = FastMath.atan(h / near)
				/ (FastMath.DEG_TO_RAD * .5f);
		float newFovY = fovY + value * 0.1f * zoomSpeed;
		if (newFovY > 0f)
		{
			// Don't let the FOV go zero or negative.
			fovY = newFovY;
		}

		h = FastMath.tan(fovY * FastMath.DEG_TO_RAD * .5f) * near;
		w = h * aspect;

		cam.setCamFrustumTop(h);
		cam.setCamFrustumBottom(-h);
		cam.setCamFrustumLeft(-w);
		cam.setCamFrustumRight(w);
	}

	protected void riseCamera(float value)
	{
		Vector3f vel = new Vector3f(0, value * moveSpeed, 0);
		Vector3f pos = cam.getLocation().clone();

		if (motionAllowed != null)
			motionAllowed.checkMotionAllowed(pos, vel);
		else
			pos.addLocal(vel);

		cam.setLocation(pos);
	}

	protected void moveCamera(float value, boolean sideways)
	{
		Vector3f vel = new Vector3f();
		Vector3f pos = cam.getLocation().clone();

		if (sideways)
		{
			cam.getLeft(vel);
		} else
		{
			cam.getDirection(vel);
		}
		vel.multLocal(value * moveSpeed);

		if (motionAllowed != null)
			motionAllowed.checkMotionAllowed(pos, vel);
		else
			pos.addLocal(vel);

		cam.setLocation(pos);
	}

	@Override
	public void onAnalog(String name, float value, float tpf)
	{
		if (!enabled)
			return;
		if (name.equals(FLYCAM_LEFT))
		{
			rotateCamera(value, initialUpVec);
		} else if (name.equals(FLYCAM_RIGHT))
		{
			rotateCamera(-value, initialUpVec);
		} else if (name.equals(FLYCAM_UP))
		{
			rotateCamera(-value * (invertY ? -1 : 1), cam.getLeft());
		} else if (name.equals(FLYCAM_DOWN))
		{
			rotateCamera(value * (invertY ? -1 : 1), cam.getLeft());
		} else if (name.equals(FLYCAM_FORWARD))
		{
			moveCamera(value, false);
		} else if (name.equals(FLYCAM_BACKWARD))
		{
			moveCamera(-value, false);
		} else if (name.equals(FLYCAM_STRAFELEFT))
		{
			moveCamera(value, true);
		} else if (name.equals(FLYCAM_STRAFERIGHT))
		{
			moveCamera(-value, true);
		} else if (name.equals(FLYCAM_RISE))
		{
			riseCamera(value);
		} else if (name.equals(FLYCAM_LOWER))
		{
			riseCamera(-value);
		} else if (name.equals(FLYCAM_ZOOMIN))
		{
			zoomCamera(value);
		} else if (name.equals(FLYCAM_ZOOMOUT))
		{
			zoomCamera(-value);
		}
	}

	@Override
	public void onAction(String name, boolean value, float tpf)
	{
		if (!enabled)
			return;

		if (name.equals(FLYCAM_ROTATEDRAG) && dragToRotate)
		{
			canRotate = value;
			inputManager.setCursorVisible(!value);
		} else if (name.equals(FLYCAM_INVERTY))
		{
			// Toggle on the up.
			if (!value)
			{
				invertY = !invertY;
			}
		}
	}

	// ChaseCamera constants
	/**
	 * Chase camera mapping for moving down. Default assigned to
	 * MouseInput.AXIS_Y direction depending on the invertYaxis configuration
	 */
	public final static String CHASECAM_DOWN = "MASSIS3" + "ChaseCamDown";
	/**
	 * Chase camera mapping for moving up. Default assigned to MouseInput.AXIS_Y
	 * direction depending on the invertYaxis configuration
	 */
	public final static String CHASECAM_UP = "MASSIS3" + "ChaseCamUp";
	/**
	 * Chase camera mapping for zooming in. Default assigned to
	 * MouseInput.AXIS_WHEEL direction positive
	 */
	public final static String CHASECAM_ZOOMIN = "MASSIS3" + "ChaseCamZoomIn";
	/**
	 * Chase camera mapping for zooming out. Default assigned to
	 * MouseInput.AXIS_WHEEL direction negative
	 */
	public final static String CHASECAM_ZOOMOUT = "MASSIS3" + "ChaseCamZoomOut";
	/**
	 * Chase camera mapping for moving left. Default assigned to
	 * MouseInput.AXIS_X direction depending on the invertXaxis configuration
	 */
	public final static String CHASECAM_MOVELEFT = "MASSIS3" + "ChaseCamMoveLeft";
	/**
	 * Chase camera mapping for moving right. Default assigned to
	 * MouseInput.AXIS_X direction depending on the invertXaxis configuration
	 */
	public final static String CHASECAM_MOVERIGHT = "MASSIS3" + "ChaseCamMoveRight";
	/**
	 * Chase camera mapping to initiate the rotation of the cam. Default
	 * assigned to MouseInput.BUTTON_LEFT and MouseInput.BUTTON_RIGHT
	 */
	public final static String CHASECAM_TOGGLEROTATE = "MASSIS3" + "ChaseCamToggleRotate";

	// fly cameara constants
	/**
	 * Fly camera mapping to look left. Default assigned to MouseInput.AXIS_X,
	 * direction negative
	 */
	public final static String FLYCAM_LEFT = "MASSIS3" + "FLYCAM_Left";
	/**
	 * Fly camera mapping to look right. Default assigned to MouseInput.AXIS_X,
	 * direction positive
	 */
	public final static String FLYCAM_RIGHT = "MASSIS3" + "FLYCAM_Right";
	/**
	 * Fly camera mapping to look up. Default assigned to MouseInput.AXIS_Y,
	 * direction positive
	 */
	public final static String FLYCAM_UP = "MASSIS3" + "FLYCAM_Up";
	/**
	 * Fly camera mapping to look down. Default assigned to MouseInput.AXIS_Y,
	 * direction negative
	 */
	public final static String FLYCAM_DOWN = "MASSIS3" + "FLYCAM_Down";
	/**
	 * Fly camera mapping to move left. Default assigned to KeyInput.KEY_A
	 */
	public final static String FLYCAM_STRAFELEFT = "MASSIS3" + "FLYCAM_StrafeLeft";
	/**
	 * Fly camera mapping to move right. Default assigned to KeyInput.KEY_D
	 */
	public final static String FLYCAM_STRAFERIGHT = "MASSIS3" + "FLYCAM_StrafeRight";
	/**
	 * Fly camera mapping to move forward. Default assigned to KeyInput.KEY_W
	 */
	public final static String FLYCAM_FORWARD = "MASSIS3" + "FLYCAM_Forward";
	/**
	 * Fly camera mapping to move backward. Default assigned to KeyInput.KEY_S
	 */
	public final static String FLYCAM_BACKWARD = "MASSIS3" + "FLYCAM_Backward";
	/**
	 * Fly camera mapping to zoom in. Default assigned to MouseInput.AXIS_WHEEL,
	 * direction positive
	 */
	public final static String FLYCAM_ZOOMIN = "MASSIS3" + "FLYCAM_ZoomIn";
	/**
	 * Fly camera mapping to zoom in. Default assigned to MouseInput.AXIS_WHEEL,
	 * direction negative
	 */
	public final static String FLYCAM_ZOOMOUT = "MASSIS3" + "FLYCAM_ZoomOut";
	/**
	 * Fly camera mapping to toggle rotation. Default assigned to
	 * MouseInput.BUTTON_LEFT
	 */
	public final static String FLYCAM_ROTATEDRAG = "MASSIS3" + "FLYCAM_RotateDrag";
	/**
	 * Fly camera mapping to move up. Default assigned to KeyInput.KEY_Q
	 */
	public final static String FLYCAM_RISE = "MASSIS3" + "FLYCAM_Rise";
	/**
	 * Fly camera mapping to move down. Default assigned to KeyInput.KEY_W
	 */
	public final static String FLYCAM_LOWER = "MASSIS3" + "FLYCAM_Lower";

	public final static String FLYCAM_INVERTY = "MASSIS3" + "FLYCAM_InvertY";

	private static String[] mappings = new String[] {
			FLYCAM_LEFT,
			FLYCAM_RIGHT,
			FLYCAM_UP,
			FLYCAM_DOWN,

			FLYCAM_STRAFELEFT,
			FLYCAM_STRAFERIGHT,
			FLYCAM_FORWARD,
			FLYCAM_BACKWARD,

			FLYCAM_ZOOMIN,
			FLYCAM_ZOOMOUT,
			FLYCAM_ROTATEDRAG,

			FLYCAM_RISE,
			FLYCAM_LOWER,

			FLYCAM_INVERTY
	};
}
