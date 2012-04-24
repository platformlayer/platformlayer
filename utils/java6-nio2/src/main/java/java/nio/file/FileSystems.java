package java.nio.file;

import java.io.IOException;
import java.net.URI;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.ServiceLoader;

public class FileSystems {

	private static ServiceLoader<FileSystemProvider> FILE_SYSTEM_PROVIDERS = ServiceLoader
			.load(FileSystemProvider.class);

	public static FileSystem newFileSystem(URI uri, Map<String, String> env) throws IOException {
		for (FileSystemProvider fileSystemProvider : getFileSystemProvider()) {
			FileSystem fileSystem = fileSystemProvider.newFileSystem(uri, env);
			if (fileSystem != null) {
				return fileSystem;
			}
		}
		throw new IOException("No file system registered for: " + uri);
	}

	private static ServiceLoader<FileSystemProvider> getFileSystemProvider() {
		return FILE_SYSTEM_PROVIDERS;
	}

}
