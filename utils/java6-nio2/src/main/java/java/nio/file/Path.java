package java.nio.file;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.util.Iterator;

public interface Path {
	Path resolve(String other);

	Path resolve(Path other);

	Path relativize(Path other);

	Path getFileName();

	Path resolveSibling(String other);

	FileSystem getFileSystem();

	boolean isAbsolute();

	Path getRoot();

	Path getParent();

	int getNameCount();

	Path getName(int index);

	Path subpath(int beginIndex, int endIndex);

	boolean startsWith(Path other);

	boolean startsWith(String other);

	boolean endsWith(Path other);

	boolean endsWith(String other);

	Path normalize();

	Path resolveSibling(Path other);

	URI toUri();

	Path toAbsolutePath();

	Path toRealPath(LinkOption[] options) throws IOException;

	File toFile();

	Iterator<Path> iterator();

	int compareTo(Path other);

	WatchKey register(WatchService watcher, Kind<?>[] events, Modifier[] modifiers) throws IOException;

	WatchKey register(WatchService watcher, Kind<?>[] events) throws IOException;

}