package org.platformlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

public class FileVisitor {
    final File baseDir;
    protected final Log log;
    private List<String> pathComponents;
    protected final File outDir;

    public FileVisitor(File srcDir, File outDir, Log log) {
        this.baseDir = srcDir;
        this.outDir = outDir;
        this.log = log;
    }

    public void start() throws MojoExecutionException {
        if (!baseDir.exists())
            return;

        if (!baseDir.isDirectory())
            throw new MojoExecutionException("Expected directory: " + baseDir);

        pathComponents = new ArrayList<String>();

        visitDirectory(baseDir);
    }

    protected void visitDirectory(File dir) throws MojoExecutionException {
        for (File file : dir.listFiles()) {
            String fileName = file.getName();
            if (file.isDirectory()) {
                List<String> savePathComponents = this.pathComponents;
                List<String> childPathComponents = new ArrayList<String>();
                childPathComponents.addAll(this.pathComponents);
                childPathComponents.add(fileName);

                this.pathComponents = childPathComponents;
                visitDirectory(file);
                this.pathComponents = savePathComponents;
            } else {
                visitFile(file);
            }
        }
    }

    protected void visitFile(File file) throws MojoExecutionException {
    }

    protected List<String> getPathComponents() {
        return pathComponents;
    }
}
