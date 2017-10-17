package com.massisframework.massis3.commons.app.server;

public interface ServerJMEApplication {

	public static String RUN_MODE_KEY = "com.massisframework.massis3.server.runMode";

	public static RunMode getGlobalRunMode()
	{
		return RunMode.valueOf(
				System.getProperty(ServerJMEApplication.RUN_MODE_KEY, RunMode.SERVER.name()));
	}

	public static enum RunMode {

		SERVER(false), DESKTOP(true), DEVELOPMENT(true);

		private boolean requiresWindow;

		RunMode(boolean requiresWindow)
		{
			this.requiresWindow = requiresWindow;
		}

		public boolean requiresWindow()
		{
			return this.requiresWindow;
		}
	}

	ServerCamera getMainCam();

}
