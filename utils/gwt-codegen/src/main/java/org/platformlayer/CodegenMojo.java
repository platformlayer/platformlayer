package org.platformlayer;

import java.io.File;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;

public abstract class CodegenMojo extends AbstractMojo {
    /**
     * Location of the file.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    protected File outputDirectory;

    /**
     * @parameter expression="${project.runtimeClasspathElements}"
     * @readonly
     */
    protected List<String> runtimeClasspathElements;

    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    protected File getProjectDir() {
        // Hack!
        File projectDir = outputDirectory.getParentFile();
        return projectDir;
    }

    protected File getCodegenDir() {
        File projectDir = getProjectDir();

        File codegenDir = new File(projectDir, "src/codegen/java");
        return codegenDir;
    }
}
