package com.massisframework.massis3.core.systems.camera;

import java.nio.ByteBuffer;

public interface VideoSequenceWriter {

	public void start(String output);

	public void writePicture(ByteBuffer rgbaBuff);

	public int getWidth();

	public void setWidth(int width);

	public int getHeight();

	public void setHeight(int height);

	public void stop();

	public default byte[] getLastImage()
	{
		return new byte[0];
	}
}
