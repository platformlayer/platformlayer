package org.platformlayer.rest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.platformlayer.ByteSourceBase;

import com.google.common.base.Charsets;

public class Utf8StringByteSource extends ByteSourceBase {
	private final byte[] bytes;

	public Utf8StringByteSource(String s) {
		this.bytes = s.getBytes(Charsets.UTF_8);
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

	@Override
	public String toString() {
		return "Utf8StringByteSource [len=" + bytes.length + "]";
	}

}
