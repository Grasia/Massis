package com.massisframework.massis3.core.systems.camera.imagewriter;

import java.nio.ByteBuffer;

public interface ImageWriter {

	public void writeImage(ByteBuffer imageData, int width, int height);
}
