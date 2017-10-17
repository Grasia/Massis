package com.massisframework.massis3.commons.app.server.impl.multi;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import com.jme3.app.SimpleApplication;
import com.jme3.renderer.ViewPort;
import com.jme3.texture.FrameBuffer;
import com.massisframework.massis3.commons.app.server.impl.AbstractServerCamera;

public class MultiApplicationCameraImpl extends AbstractServerCamera {

	public MultiApplicationCameraImpl(ViewPort vp, SimpleApplication app)
	{
		super(vp, app);
	}

	@Override
	public void internalRelease()
	{
		this.app.getRenderManager().removeMainView(this.vp);
		this.app.getRenderManager().removePostView(this.vp);
		this.app.getRenderManager().removePreView(this.vp);
	}

	int counter = 0;

	@Override
	public void postFrame(FrameBuffer out)
	{
		super.postFrame(out);
		
//		counter++;
//		Path outdir = Paths.get("/home/rpax/tmp/jme3out/" + vp.getName());
//		try
//		{
//			Files.createDirectories(outdir);
//			ImageIO.write(this.renderImage, "jpg", outdir.resolve("img_" + counter + ".jpg").toFile());
//		} catch (IOException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
	}
}
