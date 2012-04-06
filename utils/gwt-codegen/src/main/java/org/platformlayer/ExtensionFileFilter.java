package org.platformlayer;

import java.io.File;
import java.io.FileFilter;

public class ExtensionFileFilter implements FileFilter {
    final String extension;

    public ExtensionFileFilter(String extension) {
        super();
        this.extension = extension;
    }

    public boolean accept(File pathname) {
        return pathname.getName().endsWith(extension);
    }
};