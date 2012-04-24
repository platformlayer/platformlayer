package java.nio.file.spi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.util.Map;
import java.util.Set;

public abstract class FileSystemProvider {

	public abstract SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options,
			FileAttribute<?>... attrs) throws IOException;

	public abstract void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException;

	public abstract void delete(Path path) throws IOException;

	public abstract void checkAccess(Path path, AccessMode[] modes) throws IOException;

	public abstract <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options)
			throws IOException;

	public abstract DirectoryStream<Path> newDirectoryStream(Path dir, Filter<? super Path> filter) throws IOException;

	public abstract String getScheme();

	public abstract FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException;

	public abstract FileSystem getFileSystem(URI uri);

	public abstract Path getPath(URI uri);

	public abstract void copy(Path source, Path target, CopyOption... options) throws IOException;

	public abstract void move(Path source, Path target, CopyOption... options) throws IOException;

	public abstract boolean isSameFile(Path path, Path path2) throws IOException;

	public abstract boolean isHidden(Path path) throws IOException;

	public abstract FileStore getFileStore(Path path) throws IOException;

	public abstract <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type,
			LinkOption... options);

	public abstract Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options)
			throws IOException;

	public abstract void setAttribute(Path path, String attribute, Object value, LinkOption... options)
			throws IOException;

	public InputStream newInputStream(Path path, OpenOption... options) throws IOException {
		return Channels.newInputStream(Files.newByteChannel(path, options));
	}

	public OutputStream newOutputStream(Path path, OpenOption... options) throws IOException {
		return Channels.newOutputStream(Files.newByteChannel(path, options));
	}

}
