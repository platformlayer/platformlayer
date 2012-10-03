package org.platformlayer.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ByteBufferBackedInputStream extends InputStream {
	final ByteBuffer buffer;

	public ByteBufferBackedInputStream(ByteBuffer buffer) {
		this.buffer = buffer;
	}

	@Override
	public int available() throws IOException {
		return buffer.remaining();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (buffer.remaining() <= 0) {
			return -1;
		}
		if (len > buffer.remaining()) {
			len = buffer.remaining();
		}
		buffer.get(b, off, len);
		return len;
	}

	@Override
	public synchronized int read() throws IOException {
		int remaining = buffer.remaining();
		if (remaining <= 0) {
			return -1;
		}

		return buffer.get() & 0xff;
	}

}
