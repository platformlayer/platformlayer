package java.nio.file;

import java.io.IOException;

public class FileSystemException extends IOException {

	private static final long serialVersionUID = 1L;

	public FileSystemException(String message) {
		super(message);
	}

}
