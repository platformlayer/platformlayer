package java.nio.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;

public class Files {

	private static final Filter<? super Path> ACCEPT_ALL_FILTER = new Filter<Path>() {
		@Override
		public boolean accept(Path input) throws IOException {
			return true;
		}
	};

	public static InputStream newInputStream(Path path, OpenOption... options) {
		throw new UnsupportedOperationException();
		// return path.getFileSystem().provider().newInputStream(path, options);
	}

	public static <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options)
			throws IOException {
		return path.getFileSystem().provider().readAttributes(path, type, options);

	}

	public static Path createDirectory(Path path, FileAttribute<?>... attrs) throws IOException {
		path.getFileSystem().provider().createDirectory(path, attrs);
		return path;
	}

	public static long copy(InputStream src, Path dest) {
		throw new UnsupportedOperationException();
	}

	public static long copy(Path src, OutputStream dest) {
		throw new UnsupportedOperationException();
	}

	public static void delete(Path path) throws IOException {
		path.getFileSystem().provider().delete(path);
	}

	public static boolean isDirectory(Path path, LinkOption... options) {
		try {
			return readAttributes(path, BasicFileAttributes.class, options).isDirectory();
		} catch (IOException e) {
			throw new IllegalArgumentException("Error reading attributes", e);
		}
	}

	public static DirectoryStream<Path> newDirectoryStream(Path path) throws IOException {
		return path.getFileSystem().provider().newDirectoryStream(path, ACCEPT_ALL_FILTER);

	}

	public static boolean exists(Path path) {
		throw new UnsupportedOperationException();
	}

}
