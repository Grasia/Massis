package com.massisframework.massis3.simulation.server.eventbus.services;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class VertxBufferOutputStream extends OutputStream implements Buffer {

	private Buffer buf;

	/**
	 * Creates a new byte array output stream. The buffer capacity is initially
	 * 32 bytes, though its size increases if necessary.
	 */
	private VertxBufferOutputStream(Buffer buf)
	{
		this.buf = buf;
	}

	public static VertxBufferOutputStream create()
	{
		return wrap(Buffer.buffer());
	}

	public static VertxBufferOutputStream wrap(Buffer buf)
	{
		return new VertxBufferOutputStream(buf);
	}

	/**
	 * Writes the specified byte to this byte array output stream.
	 *
	 * @param b
	 *            the byte to be written.
	 */
	@Override
	public synchronized void write(int b)
	{
		buf.appendByte((byte) b);
	}

	/**
	 * Writes <code>len</code> bytes from the specified byte array starting at
	 * offset <code>off</code> to this byte array output stream.
	 *
	 * @param b
	 *            the data.
	 * @param off
	 *            the start offset in the data.
	 * @param len
	 *            the number of bytes to write.
	 */
	@Override
	public synchronized void write(byte b[], int off, int len)
	{
		if ((off < 0) || (off > b.length) || (len < 0) ||
				((off + len) - b.length > 0))
		{
			throw new IndexOutOfBoundsException();
		}
		buf.appendBytes(b, off, len);
	}

	/**
	 * Writes the complete contents of this byte array output stream to the
	 * specified output stream argument, as if by calling the output stream's
	 * write method using <code>out.write(buf, 0, count)</code>.
	 *
	 * @param out
	 *            the output stream to which to write the data.
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
	public synchronized void writeTo(OutputStream out) throws IOException
	{
		// out.write(buf, 0, count);
		out.write(buf.getBytes());
	}

	/**
	 * Returns the current size of the buffer.
	 *
	 * @return the value of the <code>count</code> field, which is the number of
	 *         valid bytes in this output stream.
	 * @see java.io.ByteArrayOutputStream#count
	 */
	public synchronized int size()
	{
		return this.buf.length();
	}

	/**
	 * Converts the buffer's contents into a string decoding bytes using the
	 * platform's default character set. The length of the new <tt>String</tt>
	 * is a function of the character set, and hence may not be equal to the
	 * size of the buffer.
	 *
	 * <p>
	 * This method always replaces malformed-input and unmappable-character
	 * sequences with the default replacement string for the platform's default
	 * character set. The {@linkplain java.nio.charset.CharsetDecoder} class
	 * should be used when more control over the decoding process is required.
	 *
	 * @return String decoded from the buffer's contents.
	 * @since JDK1.1
	 */
	@Override
	public synchronized String toString()
	{
		return buf.toString();
	}

	@Override
	public void close() throws IOException
	{
	}

	/*
	 * Delegated methods
	 */
	@Override
	public void writeToBuffer(Buffer buffer)
	{
		buf.writeToBuffer(buffer);
	}

	@Override
	public int readFromBuffer(int pos, Buffer buffer)
	{
		return buf.readFromBuffer(pos, buffer);
	}

	@Override
	public String toString(String enc)
	{
		return buf.toString(enc);
	}

	@Override
	public String toString(Charset enc)
	{
		return buf.toString(enc);
	}

	@Override
	public JsonObject toJsonObject()
	{
		return buf.toJsonObject();
	}

	@Override
	public JsonArray toJsonArray()
	{
		return buf.toJsonArray();
	}

	@Override
	public byte getByte(int pos)
	{
		return buf.getByte(pos);
	}

	@Override
	public short getUnsignedByte(int pos)
	{
		return buf.getUnsignedByte(pos);
	}

	@Override
	public int getInt(int pos)
	{
		return buf.getInt(pos);
	}

	@Override
	public int getIntLE(int pos)
	{
		return buf.getIntLE(pos);
	}

	@Override
	public long getUnsignedInt(int pos)
	{
		return buf.getUnsignedInt(pos);
	}

	@Override
	public long getUnsignedIntLE(int pos)
	{
		return buf.getUnsignedIntLE(pos);
	}

	@Override
	public long getLong(int pos)
	{
		return buf.getLong(pos);
	}

	@Override
	public long getLongLE(int pos)
	{
		return buf.getLongLE(pos);
	}

	@Override
	public double getDouble(int pos)
	{
		return buf.getDouble(pos);
	}

	@Override
	public float getFloat(int pos)
	{
		return buf.getFloat(pos);
	}

	@Override
	public short getShort(int pos)
	{
		return buf.getShort(pos);
	}

	@Override
	public short getShortLE(int pos)
	{
		return buf.getShortLE(pos);
	}

	@Override
	public int getUnsignedShort(int pos)
	{
		return buf.getUnsignedShort(pos);
	}

	@Override
	public int getUnsignedShortLE(int pos)
	{
		return buf.getUnsignedShortLE(pos);
	}

	@Override
	public int getMedium(int pos)
	{
		return buf.getMedium(pos);
	}

	@Override
	public int getMediumLE(int pos)
	{
		return buf.getMediumLE(pos);
	}

	@Override
	public int getUnsignedMedium(int pos)
	{
		return buf.getUnsignedMedium(pos);
	}

	@Override
	public int getUnsignedMediumLE(int pos)
	{
		return buf.getUnsignedMediumLE(pos);
	}

	@Override
	public byte[] getBytes()
	{
		return buf.getBytes();
	}

	@Override
	public byte[] getBytes(int start, int end)
	{
		return buf.getBytes(start, end);
	}

	@Override
	public Buffer getBytes(byte[] dst)
	{
		return buf.getBytes(dst);
	}

	@Override
	public Buffer getBytes(byte[] dst, int dstIndex)
	{
		return buf.getBytes(dst, dstIndex);
	}

	@Override
	public Buffer getBytes(int start, int end, byte[] dst)
	{
		return buf.getBytes(start, end, dst);
	}

	@Override
	public Buffer getBytes(int start, int end, byte[] dst, int dstIndex)
	{
		return buf.getBytes(start, end, dst, dstIndex);
	}

	@Override
	public Buffer getBuffer(int start, int end)
	{
		return buf.getBuffer(start, end);
	}

	@Override
	public String getString(int start, int end, String enc)
	{
		return buf.getString(start, end, enc);
	}

	@Override
	public String getString(int start, int end)
	{
		return buf.getString(start, end);
	}

	@Override
	public Buffer appendBuffer(Buffer buff)
	{
		return buf.appendBuffer(buff);
	}

	@Override
	public Buffer appendBuffer(Buffer buff, int offset, int len)
	{
		return buf.appendBuffer(buff, offset, len);
	}

	@Override
	public Buffer appendBytes(byte[] bytes)
	{
		return buf.appendBytes(bytes);
	}

	@Override
	public Buffer appendBytes(byte[] bytes, int offset, int len)
	{
		return buf.appendBytes(bytes, offset, len);
	}

	@Override
	public Buffer appendByte(byte b)
	{
		return buf.appendByte(b);
	}

	@Override
	public Buffer appendUnsignedByte(short b)
	{
		return buf.appendUnsignedByte(b);
	}

	@Override
	public Buffer appendInt(int i)
	{
		return buf.appendInt(i);
	}

	@Override
	public Buffer appendIntLE(int i)
	{
		return buf.appendIntLE(i);
	}

	@Override
	public Buffer appendUnsignedInt(long i)
	{
		return buf.appendUnsignedInt(i);
	}

	@Override
	public Buffer appendUnsignedIntLE(long i)
	{
		return buf.appendUnsignedIntLE(i);
	}

	@Override
	public Buffer appendMedium(int i)
	{
		return buf.appendMedium(i);
	}

	@Override
	public Buffer appendMediumLE(int i)
	{
		return buf.appendMediumLE(i);
	}

	@Override
	public Buffer appendLong(long l)
	{
		return buf.appendLong(l);
	}

	@Override
	public Buffer appendLongLE(long l)
	{
		return buf.appendLongLE(l);
	}

	@Override
	public Buffer appendShort(short s)
	{
		return buf.appendShort(s);
	}

	@Override
	public Buffer appendShortLE(short s)
	{
		return buf.appendShortLE(s);
	}

	@Override
	public Buffer appendUnsignedShort(int s)
	{
		return buf.appendUnsignedShort(s);
	}

	@Override
	public Buffer appendUnsignedShortLE(int s)
	{
		return buf.appendUnsignedShortLE(s);
	}

	@Override
	public Buffer appendFloat(float f)
	{
		return buf.appendFloat(f);
	}

	@Override
	public Buffer appendDouble(double d)
	{
		return buf.appendDouble(d);
	}

	@Override
	public Buffer appendString(String str, String enc)
	{
		return buf.appendString(str, enc);
	}

	@Override
	public Buffer appendString(String str)
	{
		return buf.appendString(str);
	}

	@Override
	public Buffer setByte(int pos, byte b)
	{
		return buf.setByte(pos, b);
	}

	@Override
	public Buffer setUnsignedByte(int pos, short b)
	{
		return buf.setUnsignedByte(pos, b);
	}

	@Override
	public Buffer setInt(int pos, int i)
	{
		return buf.setInt(pos, i);
	}

	@Override
	public Buffer setIntLE(int pos, int i)
	{
		return buf.setIntLE(pos, i);
	}

	@Override
	public Buffer setUnsignedInt(int pos, long i)
	{
		return buf.setUnsignedInt(pos, i);
	}

	@Override
	public Buffer setUnsignedIntLE(int pos, long i)
	{
		return buf.setUnsignedIntLE(pos, i);
	}

	@Override
	public Buffer setMedium(int pos, int i)
	{
		return buf.setMedium(pos, i);
	}

	@Override
	public Buffer setMediumLE(int pos, int i)
	{
		return buf.setMediumLE(pos, i);
	}

	@Override
	public Buffer setLong(int pos, long l)
	{
		return buf.setLong(pos, l);
	}

	@Override
	public Buffer setLongLE(int pos, long l)
	{
		return buf.setLongLE(pos, l);
	}

	@Override
	public Buffer setDouble(int pos, double d)
	{
		return buf.setDouble(pos, d);
	}

	@Override
	public Buffer setFloat(int pos, float f)
	{
		return buf.setFloat(pos, f);
	}

	@Override
	public Buffer setShort(int pos, short s)
	{
		return buf.setShort(pos, s);
	}

	@Override
	public Buffer setShortLE(int pos, short s)
	{
		return buf.setShortLE(pos, s);
	}

	@Override
	public Buffer setUnsignedShort(int pos, int s)
	{
		return buf.setUnsignedShort(pos, s);
	}

	@Override
	public Buffer setUnsignedShortLE(int pos, int s)
	{
		return buf.setUnsignedShortLE(pos, s);
	}

	@Override
	public Buffer setBuffer(int pos, Buffer b)
	{
		return buf.setBuffer(pos, b);
	}

	@Override
	public Buffer setBuffer(int pos, Buffer b, int offset, int len)
	{
		return buf.setBuffer(pos, b, offset, len);
	}

	@Override
	public Buffer setBytes(int pos, ByteBuffer b)
	{
		return buf.setBytes(pos, b);
	}

	@Override
	public Buffer setBytes(int pos, byte[] b)
	{
		return buf.setBytes(pos, b);
	}

	@Override
	public Buffer setBytes(int pos, byte[] b, int offset, int len)
	{
		return buf.setBytes(pos, b, offset, len);
	}

	@Override
	public Buffer setString(int pos, String str)
	{
		return buf.setString(pos, str);
	}

	@Override
	public Buffer setString(int pos, String str, String enc)
	{
		return buf.setString(pos, str, enc);
	}

	@Override
	public int length()
	{
		return buf.length();
	}

	@Override
	public VertxBufferOutputStream copy()
	{
		return new VertxBufferOutputStream(buf.copy());
	}

	@Override
	public VertxBufferOutputStream slice()
	{
		return new VertxBufferOutputStream(buf.slice());
	}

	@Override
	public Buffer slice(int start, int end)
	{
		return new VertxBufferOutputStream(buf.slice(start, end));
	}

	@Override
	public ByteBuf getByteBuf()
	{
		return buf.getByteBuf();
	}

}
