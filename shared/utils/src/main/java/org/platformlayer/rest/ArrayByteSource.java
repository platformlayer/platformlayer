package org.platformlayer.rest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.platformlayer.ByteSourceBase;

public class ArrayByteSource extends ByteSourceBase {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(ArrayByteSource.class);
	public static final ArrayByteSource EMPTY = new ArrayByteSource(new byte[0]);
	private final byte[] bytes;

	public ArrayByteSource(byte[] bytes) {
		this.bytes = bytes;
	}

	@Override
	public InputStream open() throws IOException {
		return new ByteArrayInputStream(bytes);
	}

	@Override
	public long getContentLength() {
		return bytes.length;
	}

	@Override
	public void close() throws IOException {
	}
}
