package org.platformlayer.rest;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.platformlayer.ByteSourceBase;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;

public class ByteStringByteSource extends ByteSourceBase {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(ByteStringByteSource.class);
	public static final ByteStringByteSource EMPTY = new ByteStringByteSource(ByteString.EMPTY);
	private final ByteString bytes;

	public ByteStringByteSource(ByteString bytes) {
		this.bytes = bytes;
	}

	public ByteStringByteSource(MessageLite bytes) {
		this(bytes.toByteString());
	}

	@Override
	public InputStream open() throws IOException {
		return bytes.newInput();
	}

	@Override
	public long getContentLength() {
		return bytes.size();
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public String toString() {
		return "ByteStringByteSource [len=" + bytes.size() + "]";
	}

}
