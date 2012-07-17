package org.platformlayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.apache.maven.plugin.MojoExecutionException;

public class Utils {

	public static String capitalize(String v) {
		return Character.toUpperCase(v.charAt(0)) + v.substring(1);
	}

	public static String readAll(File file) throws MojoExecutionException {
		try {
			StringBuilder sb = new StringBuilder();
			FileReader reader = new FileReader(file);
			try {
				char[] buffer = new char[8192];
				while (true) {
					int count = reader.read(buffer);
					if (count == -1) {
						break;
					}
					sb.append(buffer, 0, count);
				}

				return sb.toString();
			} finally {
				reader.close();
			}
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			throw new MojoExecutionException("Error reading file: " + file, e);
		}
	}

	public static void writeAll(File file, String contents) throws MojoExecutionException {
		// getLog().info("Writing file " + file);
		try {
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file));
			try {
				writer.write(contents);
			} finally {
				writer.close();
			}
		} catch (IOException e) {
			throw new MojoExecutionException("Error writing file: " + file, e);
		}
	}

	public static Class<?> getBoxedType(Class<?> type) throws MojoExecutionException {
		if (type == boolean.class)
			return Boolean.class;
		if (type == byte.class)
			return Byte.class;
		if (type == char.class)
			return Character.class;
		if (type == short.class)
			return Short.class;
		if (type == int.class)
			return Integer.class;
		if (type == long.class)
			return Long.class;
		if (type == float.class)
			return Float.class;
		if (type == double.class)
			return Double.class;

		throw new MojoExecutionException("Unhandled primitive type: " + type);
	}

	public static void mkdirs(File dir) throws MojoExecutionException {
		if (!dir.isDirectory()) {
			if (!dir.mkdirs()) {
				throw new MojoExecutionException("Error creating directories: " + dir);
			}
		}
	}

}
