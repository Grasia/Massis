package com.massisframework.massis3.commons.app.lwjgl;

import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.opengl.GL11.GL_FALSE;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.lwjgl.glfw.GLFW;

import com.jme3.input.MouseInput;
import com.jme3.input.lwjgl.GlfwMouseInput;
import com.jme3.renderer.RendererException;
import com.jme3.system.AppSettings;
import com.jme3.system.lwjgl.LwjglWindow;

/**
 * Allows multiple LWJGL Contexts. The use of locks inside this contexts is
 * based on the information present in <a href=
 * "https://web.archive.org/web/20170629104150/http://www.glfw.org/docs/latest/intro_guide.html">LWJGL
 * Guide</a>, as it was on June 29, 2017.
 * 
 * <p>
 * Although some lock operations can be skipped, (such as
 * {@link LwjglMultiWindow#create()}, They have been {@code synchronized} for
 * the sake of clarity and for avoiding future bugs that can be made on parent
 * classes. Anyway, as the locking is made in a reentrant manner, and the
 * operations are not being called too much (creating/destroying contexts),
 * shouldn't be a performance issue.
 * </p>
 * 
 * @author rpax
 *
 */
public class LwjglMultiWindow extends LwjglWindow {

	private static Object CONTEXT_LOCK = new Object();
	private static final Logger LOGGER = Logger.getLogger(LwjglMultiWindow.class.getName());

	public static String OPENGL_VERSION_RENDERER_KEY = LwjglMultiWindow.class.getName()
			+ "#Renderer";

	public LwjglMultiWindow()
	{
		super(Type.Display);
	}

	/**
	 * TODO this is a hack for preventing the
	 * {@link UnsupportedOperationException} thrown by
	 * {@link com.jme3.system.lwjgl.LwjglContext#initContextFirstTime} if
	 * renderer is not
	 */
	@Override
	protected void initContextFirstTime()
	{
		String currentRenderer = settings.getRenderer();
		if (LOGGER.isLoggable(Level.INFO))
		{
			LOGGER.info("initializating context first time");
		}
		synchronized (CONTEXT_LOCK)
		{

			try
			{
				settings.setRenderer(AppSettings.LWJGL_OPENGL3);
				super.initContextFirstTime();
			} catch (RendererException | UnsupportedOperationException e)
			{
				LOGGER.log(Level.WARNING,
						"Error when initiating context with OpenGL3: " + e.getMessage()
								+ ". Trying with OpenGL2");
				settings.setRenderer(AppSettings.LWJGL_OPENGL2);
				super.initContextFirstTime();
			} finally
			{
				settings.setRenderer(currentRenderer);
			}
		}
		if (LOGGER.isLoggable(Level.INFO))
		{
			LOGGER.info("initContextFirstTime() finished");
		}
	}

	@Override
	public void setTitle(String title)
	{
		synchronized (CONTEXT_LOCK)
		{
			super.setTitle(title);
		}
	}

	@Override
	protected void createContext(AppSettings settings)
	{
		synchronized (CONTEXT_LOCK)
		{
			super.createContext(settings);
			System.out.println("HIDING");
			glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
			GLFW.glfwHideWindow(getWindowHandle());
			// if the window is shown and we are on headless, hide it
		}
	}

	@Override
	protected void destroyContext()
	{
		synchronized (CONTEXT_LOCK)
		{
			super.destroyContext();
		}
	}

	@Override
	public void create(boolean waitFor)
	{
		// calls run() in super. Cannot be locked
		super.create(waitFor);
	}

	@Override
	protected boolean initInThread()
	{

		synchronized (CONTEXT_LOCK)
		{
			return super.initInThread();
		}
	}

	@Override
	protected void runLoop()
	{
		super.runLoop();
	}

	@Override
	protected void deinitInThread()
	{
		synchronized (CONTEXT_LOCK)
		{
			super.deinitInThread();
		}
	}

	@Override
	public void run()
	{
		// SHOULD NOT be locked. Executes an endless loop
		super.run();
	}

	@Override
	public void destroy(boolean waitFor)
	{
		synchronized (CONTEXT_LOCK)
		{
			super.destroy(waitFor);
		}
	}

	@Override
	public MouseInput getMouseInput()
	{
		if (mouseInput == null)
		{
			mouseInput = new ConcurrentGlfwMouseInput(this);
		}
		return mouseInput;
	}

}
