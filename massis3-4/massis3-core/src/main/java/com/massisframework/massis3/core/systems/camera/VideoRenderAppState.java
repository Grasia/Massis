package com.massisframework.massis3.core.systems.camera;

import java.util.List;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.post.SceneProcessor;
import com.jme3.renderer.ViewPort;
import com.jme3.texture.FrameBuffer;
import com.jme3.util.SafeArrayList;

public class VideoRenderAppState extends AbstractAppState {

	private List<RenderOffScreenProcessor> processors;

	public VideoRenderAppState()
	{
		this.processors = new SafeArrayList<>(RenderOffScreenProcessor.class);
	}

	@Override
	public void initialize(AppStateManager stateManager, Application app)
	{
		super.initialize(stateManager, app);
	}

	public void recordViewPort(ViewPort vp, String output)
	{
		this.recordViewPort(vp, output, vp.getOutputFrameBuffer());
	}

	public void recordViewPort(ViewPort vp, String output, FrameBuffer ofb)
	{
		RenderOffScreenProcessor processor = new RenderOffScreenProcessor(output);
		vp.addProcessor(processor);
		this.processors.add(processor);

	}

	@Override
	public void update(float tpf)
	{
		super.update(tpf);
		// Ensure every OffScreenProcessor is the last one of each viewport.
		for (RenderOffScreenProcessor osp : processors)
		{
			if (osp.isInitialized())
			{
				ViewPort vp = osp.getViewPort();
				ensureProcessorIsLast(vp);
			}
		}
	}

	protected void ensureProcessorIsLast(ViewPort vp)
	{
		final List<SceneProcessor> processors = vp.getProcessors();

		int processorIndex = -1;
		for (int i = 0; i < processors.size(); i++)
		{
			SceneProcessor processor = processors.get(i);
			if (processor instanceof RenderOffScreenProcessor)
			{
				if (processorIndex != -1)
				{
					throw new IllegalStateException(
							"ViewPort cannot have two processors of type "
									+ RenderOffScreenProcessor.class.getName());
				}
				processorIndex = i;
			}
		}
		if (processorIndex == -1)
		{
			throw new IllegalStateException(
					"Viewport does not have any screenProcessor of type "
							+ RenderOffScreenProcessor.class.getName()
							+ ". Was it removed elsewhere?");
		}
		if (processorIndex > 0)
		{
			// swap.
			SceneProcessor recorder = processors.remove(processorIndex);
			processors.add(recorder);
		}
	}

	@Override
	public void cleanup()
	{
		super.cleanup();
		this.processors.forEach(p -> p.cleanup());
	}

	@Override
	public void postRender()
	{
		super.postRender();

	}

}
