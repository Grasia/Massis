package com.massisframework.massis3.commons.app.server;

import com.jme3.scene.Node;

public interface MassisSystem {

	void stateAttached(AppSystemManager stateManager);

	void initialize(AppSystemManager stateManager);

	void update(float tpf);

	void graphicalUpdate(Node systemNode, float tpf);

	void cleanup();

	void setEnabled(boolean enabled);

	boolean isEnabled();

	@Deprecated
	public void runPendingTasks(Node systemNode);

	@Deprecated
	void postRender();

}
