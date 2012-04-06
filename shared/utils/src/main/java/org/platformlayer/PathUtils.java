package org.platformlayer;

import java.io.File;

public class PathUtils {
    public static String getFileName(String path) {
        String filename = path;
        if (filename.contains(File.separator)) {
            filename = filename.substring(filename.lastIndexOf(File.separatorChar) + 1);
        }
        return filename;
    }

    public static String getDirectoryName(String path) {
        String dir = path;
        if (!dir.contains(File.separator)) {
            return null;
        }

        dir = dir.substring(0, dir.lastIndexOf(File.separatorChar));
        return dir;
    }
}
