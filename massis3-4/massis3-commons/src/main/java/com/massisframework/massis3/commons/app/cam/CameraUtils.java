package com.massisframework.massis3.commons.app.cam;

import java.util.List;
import java.util.function.Consumer;

import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;

public class CameraUtils {

	public static ViewPort getViewPortByName(RenderManager rm, String name)
	{
		ViewPort vp = null;
		if (vp == null)
			vp = getVPByName(rm.getPreViews(), name);
		if (vp == null)
			vp = getVPByName(rm.getMainViews(), name);
		if (vp == null)
			vp = getVPByName(rm.getPostViews(), name);
		return vp;

	}

	public static void forEachViewPort(RenderManager rm, Consumer<ViewPort> action)
	{
		rm.getPreViews().forEach(action);
		rm.getMainViews().forEach(action);
		rm.getPostViews().forEach(action);
	}

	public static ViewPort getCurrentViewPort(RenderManager rm)
	{
		ViewPort vp = null;
		Camera cam = rm.getCurrentCamera();
		if (vp == null)
			vp = getVPByCamera(rm.getPreViews(), cam);
		if (vp == null)
			vp = getVPByCamera(rm.getMainViews(), cam);
		if (vp == null)
			vp = getVPByCamera(rm.getPostViews(), cam);
		return vp;

	}

	private static ViewPort getVPByCamera(List<ViewPort> vps, Camera camera)
	{
		return vps.stream()
				.filter(vp -> vp.getCamera() == camera)
				.findFirst().orElse(null);
	}

	private static ViewPort getVPByName(List<ViewPort> vps, String name)
	{
		return vps.stream()
				.filter(vp -> vp.getName().equals(name))
				.findFirst().orElse(null);
	}
	public static Camera newCamera(int width, int height)
	{
		Camera cam = new Camera(width, height);
		cam.setFrustumPerspective(45f, (float) cam.getWidth() / cam.getHeight(), 1f, 1000f);
		cam.setLocation(new Vector3f(0f, 0f, 10f));
		cam.lookAt(new Vector3f(0f, 0f, 0f), Vector3f.UNIT_Y);
		return cam;
	}
	public static final void flip_Y_RGB(byte[] rgbImageData, int width, int height)
	{
		int inPtr, outPtr, y, x;
		byte r2, g2, b2;

		for (y = 0; y < height / 2; y++)
		{
			for (x = 0; x < width; x++)
			{
				inPtr = (y * width + x) * 3;
				outPtr = ((height - y - 1) * width + x) * 3;

				r2 = rgbImageData[outPtr + 0];
				g2 = rgbImageData[outPtr + 1];
				b2 = rgbImageData[outPtr + 2];

				rgbImageData[outPtr + 0] = rgbImageData[inPtr + 0];
				rgbImageData[outPtr + 1] = rgbImageData[inPtr + 1];
				rgbImageData[outPtr + 2] = rgbImageData[inPtr + 2];

				rgbImageData[inPtr + 0] = r2;
				rgbImageData[inPtr + 1] = g2;
				rgbImageData[inPtr + 2] = b2;

			}
		}
	}
	
	public static final void flip_Y_BGR(byte[] rgbImageData, int width, int height)
	{
		int inPtr, outPtr, y, x;
		byte r2, g2, b2;

		for (y = 0; y < height / 2; y++)
		{
			for (x = 0; x < width; x++)
			{
				inPtr = (y * width + x) * 3;
				outPtr = ((height - y - 1) * width + x) * 3;

				b2 = rgbImageData[outPtr + 0];
				g2 = rgbImageData[outPtr + 1];
				r2 = rgbImageData[outPtr + 2];

				rgbImageData[outPtr + 0] = rgbImageData[inPtr + 0];
				rgbImageData[outPtr + 1] = rgbImageData[inPtr + 1];
				rgbImageData[outPtr + 2] = rgbImageData[inPtr + 2];

				rgbImageData[inPtr + 0] = b2;
				rgbImageData[inPtr + 1] = g2;
				rgbImageData[inPtr + 2] = r2;

			}
		}
	}
}
