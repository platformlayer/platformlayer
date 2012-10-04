package org.platformlayer;

import java.io.IOException;

public interface ByteSink {
	void beginData(int length) throws IOException;

	void addBytes(byte[] data, int offset, int length) throws IOException;

	void endData() throws IOException;
}
