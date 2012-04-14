package org.platformlayer;

import java.io.File;

public class FilePath {
	private final String value;

	public FilePath(String value) {
		this.value = value;
	}

	public FilePath(File file) {
		this(file.getAbsolutePath());
	}

	public static final FilePath filePath(String value) {
		return new FilePath(value);
	}

	public FilePath join(String relativePath) {
		String newValue = value;
		if (!newValue.endsWith("/") && !relativePath.startsWith("/")) {
			newValue += "/";
		}
		newValue += relativePath;
		return new FilePath(newValue);
	}

	public String asString() {
		return value;
	}

	public File asFile() {
		return new File(asString());
	}

	public FilePath normalize() {
		String path = this.value;
		while (path.contains("//")) {
			path = path.replace("//", "/");
		}
		if (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		return new FilePath(path);
	}

	public String getFileName() {
		String path = asString();
		int lastSlash = path.lastIndexOf('/');
		if (lastSlash != -1) {
			path = path.substring(lastSlash + 1);
		}
		return path;
	}

	public FilePath getParentDirectory() {
		return new FilePath(PathUtils.getDirectoryName(this.asString()));
	}

	public FilePath replace(String find, String replace) {
		return new FilePath(this.value.replace(find, replace));
	}

	public boolean isBeneath(FilePath otherPath) {
		// end all paths with / to stop the problem of vol/abc vol/abcd.
		String thisPathString = this.asString();
		if (!thisPathString.endsWith("/")) {
			thisPathString = thisPathString + "/";
		}

		String otherPathString = otherPath.asString();
		if (!otherPathString.endsWith("/")) {
			otherPathString = otherPathString + "/";
		}

		return thisPathString.startsWith(otherPathString);
	}

}
