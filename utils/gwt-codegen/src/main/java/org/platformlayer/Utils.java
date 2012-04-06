package org.platformlayer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.apache.maven.plugin.MojoExecutionException;

public class Utils {

    public static String capitalize(String v) {
        return Character.toUpperCase(v.charAt(0)) + v.substring(1);
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
