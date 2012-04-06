package org.platformlayer.ops.crypto;

import java.io.File;
import java.io.FilenameFilter;

public class FilenameEndsWithFilter implements FilenameFilter {

    final String suffix;

    public FilenameEndsWithFilter(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public boolean accept(File dir, String name) {
        return name.endsWith(suffix);
    }

}
