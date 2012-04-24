package java.nio.channels;

import java.io.IOException;

public interface SeekableByteChannel extends ByteChannel {

	long position() throws IOException;

	SeekableByteChannel position(long newPosition) throws IOException;

	long size() throws IOException;

	SeekableByteChannel truncate(long size) throws IOException;

}
