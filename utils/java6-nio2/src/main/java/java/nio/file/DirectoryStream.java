package java.nio.file;

import java.io.Closeable;
import java.io.IOException;

public interface DirectoryStream<T> extends Iterable<T>, Closeable {
	public static interface Filter<V> {
		boolean accept(V input) throws IOException;
	}
}
