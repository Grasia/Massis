package com.massisframework.massis3.core.systems.camera.imagewriter;

public interface RenderImageListener {

	public void renderReady(String vpName);

	public boolean requiresNewFrame(String vpName);

	public void renderFinished();
}
