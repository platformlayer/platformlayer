package java.nio.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.HashSet;
import java.util.Set;

public class Files {

	private static final Filter<? super Path> ACCEPT_ALL_FILTER = new Filter<Path>() {
		@Override
		public boolean accept(Path input) throws IOException {
			return true;
		}
	};

	public static InputStream newInputStream(Path path, OpenOption... options) throws IOException {
		return path.getFileSystem().provider().newInputStream(path, options);
	}

	public static OutputStream newOutputStream(Path path, OpenOption... options) throws IOException {
		return path.getFileSystem().provider().newOutputStream(path, options);
	}

	public static <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options)
			throws IOException {
		return path.getFileSystem().provider().readAttributes(path, type, options);

	}

	public static Path createDirectory(Path path, FileAttribute<?>... attrs) throws IOException {
		path.getFileSystem().provider().createDirectory(path, attrs);
		return path;
	}

	static long copyStreams(InputStream is, OutputStream os) throws IOException {
		long count = 0;
		byte[] buffer = new byte[32768];
		while (true) {
			int bytesRead = is.read(buffer);
			if (bytesRead == -1) {
				break;
			}

			os.write(buffer, 0, bytesRead);

			count += bytesRead;
		}
		return count;
	}

	public static long copy(InputStream src, Path dest) throws IOException {
		OutputStream os = newOutputStream(dest, StandardOpenOption.WRITE);
		try {
			return copyStreams(src, os);
		} finally {
			os.close();
		}
	}

	public static long copy(Path src, OutputStream dest) throws IOException {
		InputStream is = newInputStream(src, StandardOpenOption.READ);
		try {
			return copyStreams(is, dest);
		} finally {
			is.close();
		}
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
		try {
			// This is crappy, but this is the contract...
			/* BasicFileAttributes attributes = */readAttributes(path, BasicFileAttributes.class);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public static SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options,
			FileAttribute<?>... attrs) throws IOException {
		return path.getFileSystem().provider().newByteChannel(path, options, attrs);
	}

	public static SeekableByteChannel newByteChannel(Path path, OpenOption... options) throws IOException {
		Set<OpenOption> set = new HashSet<OpenOption>();
		for (OpenOption option : options) {
			set.add(option);
		}
		return newByteChannel(path, set);
	}

}
