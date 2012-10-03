package org.platformlayer.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class ByteBufferBackedOutputStream extends OutputStream {
	final ByteBuffer buffer;

	public ByteBufferBackedOutputStream(ByteBuffer buffer) {
		this.buffer = buffer;
	}

	@Override
	public synchronized void write(int b) throws IOException {
		buffer.put((byte) b);
	}

	@Override
	public synchronized void write(byte[] b, int off, int len) throws IOException {
		buffer.put(b, off, len);
	}

}