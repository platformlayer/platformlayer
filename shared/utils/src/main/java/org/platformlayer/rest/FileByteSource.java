package org.platformlayer.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.platformlayer.ByteSourceBase;

public class FileByteSource extends ByteSourceBase {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(FileByteSource.class);
	private final File file;

	public FileByteSource(File file) {
		this.file = file;
	}

	@Override
	public InputStream open() throws IOException {
		return new FileInputStream(file);
	}

	@Override
	public long getContentLength() {
		return this.file.length();
	}

	@Override
	public void close() throws IOException {
	}
}
