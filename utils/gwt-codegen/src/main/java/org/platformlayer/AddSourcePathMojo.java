package org.platformlayer;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * 
 * @goal addsourcepath
 * 
 * @phase generate-sources
 * 
 * @requiresDependencyResolution
 */
public class AddSourcePathMojo extends CodegenMojo {

    public void execute() throws MojoExecutionException {
        File codegenDir = getCodegenDir();

        getLog().info("Adding source: " + codegenDir.getAbsolutePath());
        this.project.addCompileSourceRoot(codegenDir.getAbsolutePath());
    }

}
