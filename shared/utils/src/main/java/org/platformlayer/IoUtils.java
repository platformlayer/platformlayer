package org.platformlayer;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

/**
 * Utility functions to do with IO
 * 
 * @author justinsb
 * 
 */
@Deprecated
// Move to fathomdb-commons
public class IoUtils {
	static final Logger log = LoggerFactory.getLogger(IoUtils.class);

	static final Random random = new Random();

	public static void copyRecursive(File srcDir, File destDir) throws IOException {
		for (File srcFile : srcDir.listFiles()) {
			File destFile = new File(destDir.getAbsolutePath() + File.separator + srcFile.getName());

			if (srcFile.isDirectory()) {
				destFile.mkdir();
				copyRecursive(srcFile, destFile);
			} else {
				copyFile(srcFile, destFile);
			}
		}
	}

	public static void copyFile(File srcFile, File destFile) throws IOException {
		FileOutputStream fos = new FileOutputStream(destFile);
		try {
			copyToOutputStream(srcFile, fos);
		} finally {
			fos.close();
		}

	}

	public static void copyToOutputStream(File file, OutputStream outputStream) throws IOException {
		InputStream is = new FileInputStream(file);
		try {
			copyToOutputStream(is, outputStream);
		} finally {
			is.close();
		}

	}

	public static void copyToOutputStream(InputStream is, OutputStream os) throws IOException {
		byte[] buffer = new byte[32768];
		while (true) {
			int bytesRead = is.read(buffer);
			if (bytesRead == -1) {
				break;
			}

			os.write(buffer, 0, bytesRead);
		}
	}

	public static String getRelativePath(File baseDir, File file) {
		String basePath = baseDir.getAbsolutePath();
		if (!basePath.endsWith(File.separator)) {
			basePath += File.separator;
		}

		String filePath = file.getAbsolutePath();
		if (!filePath.startsWith(basePath)) {
			throw new IllegalArgumentException("Not dir-relative: " + filePath + " vs " + basePath);
		}

		String relativePath = filePath.substring(basePath.length());
		return relativePath;
	}

	public static void rmdirRecursive(File parent) {
		if (!parent.exists()) {
			return;
		}

		for (File child : parent.listFiles()) {
			if (child.isDirectory()) {
				rmdirRecursive(child);
			} else {
				if (!child.delete()) {
					throw new IllegalArgumentException("Could not delete file: " + child);
				}
			}
		}

		if (!parent.delete()) {
			throw new IllegalArgumentException("Could not delete dir: " + parent);
		}
	}

	public static long calculateTotalSize(File directory) {
		long totalFileSize = 0;
		for (File file : directory.listFiles()) {
			if (file.isFile()) {
				totalFileSize += file.length();
			} else {
				totalFileSize += calculateTotalSize(file);
			}
		}
		return totalFileSize;
	}

	public static List<File> findAllFiles(File baseDirectory, boolean includeDirectories) {
		List<File> dest = new ArrayList<File>();
		findAllFiles(baseDirectory, includeDirectories, dest);
		return dest;
	}

	private static void findAllFiles(File directory, boolean includeDirectories, List<File> dest) {
		for (File file : directory.listFiles()) {
			if (file.isFile()) {
				dest.add(file);
			} else {
				if (includeDirectories) {
					dest.add(file);
				}

				findAllFiles(file, includeDirectories, dest);
			}
		}
	}

	public static void safeClose(Closeable closeable) {
		if (closeable == null) {
			return;
		}
		try {
			closeable.close();
		} catch (IOException e) {
			log.error("Ignoring unexpected error closing item", e);
		}
	}

	public static void safeClose(ServerSocket closeable) {
		if (closeable == null) {
			return;
		}
		try {
			closeable.close();
		} catch (IOException e) {
			log.error("Ignoring unexpected error closing item", e);
		}
	}

	public static void safeClose(Socket closeable) {
		if (closeable == null) {
			return;
		}
		try {
			closeable.close();
		} catch (IOException e) {
			log.error("Ignoring unexpected error closing item", e);
		}
	}

	public static void writeAllBinary(File file, byte[] data) throws IOException {
		FileOutputStream writer = new FileOutputStream(file);
		try {
			if (data != null) {
				writer.write(data);
			}
			writer.flush();
		} finally {
			writer.close();
		}
	}

	public static byte[] readAllBinary(InputStream input) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		com.fathomdb.io.IoUtils.copyStream(input, output);
		return output.toByteArray();
	}

	public static byte[] readAllBinary(FileInputStream input) throws IOException {
		long fileSize = input.getChannel().size();
		int bytesToRead = (int) fileSize;

		byte[] data = new byte[bytesToRead];
		int offset = 0;
		while (bytesToRead > 0) {
			int bytesRead = input.read(data, offset, bytesToRead);
			if (bytesRead <= 0) {
				throw new IllegalStateException("Cannot read file fully");
			}
			bytesToRead -= bytesRead;
			offset += bytesRead;
		}

		return data;
	}

	// @Deprecated
	// // Consider using the atomic version instead
	// public static long copyStreamToFile(InputStream input, File outputFile) throws IOException {
	// FileOutputStream fos = new FileOutputStream(outputFile);
	// try {
	// return copyStream(input, fos);
	// } finally {
	// IoUtils.safeClose(fos);
	// }
	// }
	//
	// public static long copyStreamToFileAtomic(InputStream input, File outputFile) throws IOException {
	// AtomicFileOutputStream fos = AtomicFileOutputStream.build(outputFile);
	// try {
	// long retval = copyStream(input, fos);
	// fos.doCommit();
	// return retval;
	// } finally {
	// IoUtils.safeClose(fos);
	// }
	// }

	public static String readAll(Reader in) throws IOException {
		StringBuilder contents = new StringBuilder();

		char[] buffer = new char[8192];
		while (true) {
			int readCount = in.read(buffer);
			if (readCount == -1) {
				break;
			}
			contents.append(buffer, 0, readCount);
		}

		return contents.toString();
	}

	/**
	 * This closes the stream, so you don't have to worry about it
	 * 
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	public static String readAll(InputStream inputStream) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, Charsets.UTF_8));
		try {
			return readAll(in);
		} finally {
			IoUtils.safeClose(in);
		}
	}

	public static byte[] readAllBinary(File file) throws IOException {
		FileInputStream in = new FileInputStream(file);
		try {
			return readAllBinary(in);
		} finally {
			IoUtils.safeClose(in);
		}
	}

	/**
	 * Calls mkdirs, and checks return code, keeping findbugs happy.
	 * 
	 * @param directory
	 */
	public static void safeMkDirs(File directory) {
		if (!directory.exists()) {
			if (!directory.mkdirs()) {
				throw new IllegalStateException("Could not create directory: " + directory.getAbsolutePath());
			}
		}
	}

	public static void deleteDirectoryRecursive(File directory) {
		for (File child : directory.listFiles()) {
			if (child.isDirectory()) {
				deleteDirectoryRecursive(child);
			} else {
				child.delete();
			}
		}
	}

	public static void renameReplace(File src, File dest) throws IOException {
		if (isWindows()) {
			com.fathomdb.io.IoUtils.safeDelete(dest);
		}
		if (!src.renameTo(dest)) {
			throw new IOException("Error renaming file " + src + " to " + dest);
		}

		// FileInputStream fis = null;
		// FileOutputStream fos = null;
		// try {
		// fis = new FileInputStream(src);
		// fos = new FileOutputStream(dest);
		//
		// copyStream(fis, fos);
		// } finally {
		// IoUtils.safeClose(fos);
		// IoUtils.safeClose(fis);
		// }
	}

	public static void safeMkdir(File directory) {
		if (!directory.exists()) {
			if (!directory.mkdir()) {
				throw new IllegalStateException("Could not create directory: " + directory.getAbsolutePath());
			}
		}
	}

	/**
	 * Creates a temporary file in a specified directory; useful because that way we can move files using File.rename
	 * without crossing volumes
	 * 
	 * @param directory
	 * @param prefix
	 * @param suffix
	 * @return
	 * @throws IOException
	 */
	public static File createTempFile(File directory, String prefix, String suffix) throws IOException {
		while (true) {
			String name;
			synchronized (random) {
				name = prefix + Long.toHexString(random.nextLong()) + suffix;
			}
			File file = new File(directory, name);
			try {
				if (file.createNewFile()) {
					return file;
				}
			} catch (IOException e) {
				log.error("Failed to create temp file: " + file, e);
				throw e;
			}
		}
	}

	public static File getTempDir() {
		String tmpDir = System.getProperty("java.io.tmpdir");
		File directory = new File(tmpDir);
		return directory;
	}

	public static File createTempDir(String prefix, String suffix) {
		File directory = getTempDir();
		synchronized (random) {
			while (true) {
				String name = prefix + random.nextInt() + suffix;
				File file = new File(directory, name);
				if (!file.exists()) {
					if (file.mkdir()) {
						return file;
					}
				}
			}
		}
	}

	public static boolean filesAreIdentical(File file1, File file2) throws IOException {
		if (!file1.exists()) {
			return false;
		}
		if (!file2.exists()) {
			return false;
		}

		if (file1.length() != file2.length()) {
			return false;
		}

		byte[] file1Contents = readAllBinary(file1);
		byte[] file2Contents = readAllBinary(file2);

		return (Arrays.equals(file1Contents, file2Contents));
	}

	public static void copyToStream(ByteBuffer byteBuffer, OutputStream os) throws IOException {
		byte[] buffer = new byte[32768];
		while (true) {
			int byteCount = Math.min(byteBuffer.remaining(), buffer.length);
			if (byteCount == 0) {
				break;
			}

			byteBuffer.get(buffer, 0, byteCount);
			os.write(buffer, 0, byteCount);
		}
	}

	public static boolean isWindows() {
		String osName = System.getProperty("os.name");
		return (osName.contains("Windows"));
	}

	public static String readAllResource(Class<?> context, String resourceName) throws IOException {
		InputStream resourceAsStream = context.getResourceAsStream(resourceName);
		String text;
		try {
			text = IoUtils.readAll(resourceAsStream);
			return text;
		} finally {
			IoUtils.safeClose(resourceAsStream);
		}
	}

	public static void readFully(InputStream is, byte[] buffer, int off, int len) throws IOException {
		while (len > 0) {
			int bytesRead = is.read(buffer, off, len);
			if (len < 0) {
				throw new IOException("Encountered end-of-stream");
			}
			off += bytesRead;
			len -= bytesRead;
		}
	}

	// public static void writeAllAtomic(File file, String contents) throws IOException {
	// AtomicFileOutputStream atomicOutputStream = AtomicFileOutputStream.build(file);
	// OutputStreamWriter writer = new OutputStreamWriter(atomicOutputStream);
	// try {
	// writer.write(contents);
	// writer.flush();
	// atomicOutputStream.doCommit();
	// } finally {
	// IoUtils.safeClose(writer);
	// }
	// }
	//
	// public static void writeAllAtomic(File file, byte[] data) throws IOException {
	// AtomicFileOutputStream fos = AtomicFileOutputStream.build(file);
	// try {
	// if (data != null) {
	// fos.write(data);
	// }
	// fos.doCommit();
	// } finally {
	// IoUtils.safeClose(fos);
	// }
	// }

	public static Iterable<String> readLines(File file, boolean skipBlanks, boolean skipComments) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		try {
			return readLines(fis, skipBlanks, skipComments);
		} finally {
			IoUtils.safeClose(fis);
		}
	}

	public static Iterable<String> readLines(InputStream is, boolean skipBlanks, boolean skipComments)
			throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, Charsets.UTF_8));
		try {
			// TODO: This could avoid buffering if we ever start using this for huge files
			List<String> lines = new ArrayList<String>();
			while (true) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				String trimmed = line.trim();
				if (skipComments && trimmed.startsWith("#")) {
					continue;
				}
				if (skipBlanks && trimmed.length() == 0) {
					continue;
				}
				lines.add(line);
			}
			return lines;
		} finally {
			IoUtils.safeClose(reader);
		}
	}

	// public static InputStream openFileSegment(File file, long segmentOffset, long segmentLength) throws IOException {
	// InputStream is = null;
	// try {
	// is = new FileInputStream(file);
	//
	// long remaining = file.length();
	//
	// if (segmentOffset != 0) {
	// is.skip(segmentOffset);
	// remaining -= segmentOffset;
	// }
	//
	// if (segmentLength != remaining) {
	// if (segmentLength > remaining) {
	// log.warn("File segment requested that was greater than bytes available, returning shorter segment");
	// segmentLength = remaining;
	// }
	//
	// is = new LimitLengthInputStream(is, segmentLength);
	// }
	//
	// InputStream retval = is;
	// // Don't close in normal operation
	// is = null;
	// return retval;
	// } finally {
	// IoUtils.safeClose(is);
	// }
	// }

}
