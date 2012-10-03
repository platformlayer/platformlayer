package org.platformlayer;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public interface ByteSource extends Closeable {
	InputStream open() throws IOException;

	long getContentLength() throws IOException;

	ByteMetadata getMetadata();
}
