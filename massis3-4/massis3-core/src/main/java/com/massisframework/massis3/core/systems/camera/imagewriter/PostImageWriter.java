package com.massisframework.massis3.core.systems.camera.imagewriter;

import static com.massisframework.massis3.commons.collections.StreamUtil.concat;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.ViewPort;
import com.jme3.util.BufferUtils;
import com.jme3.util.Screenshots;

/**
 * TODO not ready
 * 
 * @author rpax
 *
 */
public class PostImageWriter {

	private static ThreadLocal<ByteBuffer> outBuf_TL = new ThreadLocal<>();
	private static ThreadLocal<byte[]> writeCpuBuff_TL = new ThreadLocal<>();
	private static ThreadLocal<BufferedImage> buffImage_TL = new ThreadLocal<>();

	public PostImageWriter()
	{

	}

	private BufferedImage getBuffImage(int width, int height)
	{
		BufferedImage bi = buffImage_TL.get();
		if (bi == null || bi.getWidth() != width || bi.getHeight() != height)
		{
			bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
			buffImage_TL.set(bi);
		}
		return bi;
	}

	private ByteBuffer getOutBufTL(int width, int height)
	{
		ByteBuffer bb = outBuf_TL.get();
		bb = BufferUtils.ensureLargeEnough(bb, width * height * 4);
		bb.rewind();
		bb.limit(width * height * 4);
		outBuf_TL.set(bb);
		return bb;
	}

	private byte[] getWriteCpuBufTL(int width, int height)
	{
		byte[] bb = writeCpuBuff_TL.get();
		if (bb == null || bb.length < width * height * 4)
		{
			bb = new byte[width * height * 4];
			writeCpuBuff_TL.set(bb);
		}
		return bb;
	}

	private ViewPort getViewPortOf(Camera cam, RenderManager rm)
	{
		return concat(rm.getMainViews(), rm.getPostViews(), rm.getPreViews())
				.filter(vp -> vp.getCamera() == cam)
				.findAny()
				.orElse(null);
	}

	public BufferedImage postFrame(
			RenderManager rm,
			BufferedImage outImage)
			throws Exception
	{
		/**
		 * @formatter:off
		 */
		Camera curCamera = rm.getCurrentCamera();
		ViewPort vp=getViewPortOf(curCamera,rm);
		int width = curCamera.getWidth();
		int height = curCamera.getHeight();
		int viewX =      (int) (curCamera.getViewPortLeft()   * curCamera.getWidth());
		int viewY =      (int) (curCamera.getViewPortBottom() * curCamera.getHeight());
		int viewWidth =  (int) ((curCamera.getViewPortRight() - curCamera.getViewPortLeft()) * curCamera.getWidth());
		int viewHeight = (int) ((curCamera.getViewPortTop()   - curCamera.getViewPortBottom())* curCamera.getHeight());
		Renderer renderer = rm.getRenderer();
		ByteBuffer outBuf = getOutBufTL(curCamera.getWidth(), curCamera.getHeight());
		/**
		 * @formatter:on
		 */
		// renderer.setViewPort(0, 0, width, height);
		renderer.readFrameBuffer(vp.getOutputFrameBuffer(), outBuf);
		flipY(outBuf, width, height);
		// No hace falta
		// renderer.setViewPort(viewX, viewY, viewWidth, viewHeight);
		if (outImage == null || outImage.getWidth() != width || outImage.getHeight() != height)
		{
			outImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
		}
		Screenshots.convertScreenShot2(outBuf.asIntBuffer(), outImage);
		return outImage;
		// byte[] writeCpuBuff = getWriteCpuBufTL(width, height);
		// outBuf.get(writeCpuBuff);
		// InputStream input = new ByteArrayInputStream(writeCpuBuff, 0, width *
		// height * 4);
		//
		// BufferedImage bImageFromConvert = ImageIO.read(input);
		// ImageIO.write(bImageFromConvert, "jpg", outputFile);

	}

	public static void convertScreenShot2(IntBuffer bgraBuf, BufferedImage out)
	{
		WritableRaster wr = out.getRaster();
		DataBufferInt db = (DataBufferInt) wr.getDataBuffer();

		int[] cpuArray = db.getData();

		bgraBuf.clear();
		bgraBuf.get(cpuArray);

		// int width = wr.getWidth();
		// int height = wr.getHeight();
		//
		// // flip the components the way AWT likes them
		// for (int y = 0; y < height / 2; y++){
		// for (int x = 0; x < width; x++){
		// int inPtr = (y * width + x);
		// int outPtr = ((height-y-1) * width + x);
		// int pixel = cpuArray[inPtr];
		// cpuArray[inPtr] = cpuArray[outPtr];
		// cpuArray[outPtr] = pixel;
		// }
		// }
	}

	private static void flipY(ByteBuffer buf, int width, int height)
	{
		for (int y = 0; y < height / 2; y++)
		{
			for (int x = 0; x < width; x++)
			{
				int inPtr0 = (y * width + x) * 4;
				int outPtr0 = ((height - y - 1) * width + x) * 4;
				//

				byte r1 = buf.get(inPtr0 + 0);
				byte g1 = buf.get(inPtr0 + 1);
				byte b1 = buf.get(inPtr0 + 2);
				byte a1 = buf.get(inPtr0 + 3);

				byte r2 = buf.get(outPtr0 + 0);
				byte g2 = buf.get(outPtr0 + 1);
				byte b2 = buf.get(outPtr0 + 2);
				byte a2 = buf.get(outPtr0 + 3);

				// cpuArray[outPtr+0] = a1;
				buf.put(outPtr0 + 0, r1);
				buf.put(outPtr0 + 1, g1);
				buf.put(outPtr0 + 2, b1);
				buf.put(outPtr0 + 3, a1);

				buf.put(inPtr0 + 0, r2);
				buf.put(inPtr0 + 1, g2);
				buf.put(inPtr0 + 2, b2);
				buf.put(inPtr0 + 3, a2);
			}
		}
	}
}
