package org.platformlayer;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Goal which touches a timestamp file.
 * 
 * @goal generate
 * 
 * @phase process-sources,generate-sources
 * 
 * @requiresDependencyResolution
 */
public class GwtCodegenMojo extends AbstractMojo {
    /**
     * Location of the file.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory;

    /**
     * @parameter expression="${project.runtimeClasspathElements}"
     * @readonly
     */
    private List<String> runtimeClasspathElements;

    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    private TemplateEngine template;

    public void execute() throws MojoExecutionException {
        // File f = outputDirectory;

        // File generateDir = new File(outputDirectory, "generated-sources/gwt");

        // Hack!
        File projectDir = outputDirectory.getParentFile();
        File codegenDir = new File(projectDir, "src/codegen/java");

        ClassLoader classLoader = buildClassloader();
        ClassInspection classInspection = new ClassInspection(classLoader);
        template = new TemplateEngine(getClass().getClassLoader(), getLog());

        // getLog().info("Adding source: " + codegenDir.getAbsolutePath());
        // this.project.addCompileSourceRoot(codegenDir.getAbsolutePath());

        File targetClasses = new File(outputDirectory, "classes");

        GwtCodegenFileVisitor visitor = new GwtCodegenFileVisitor(targetClasses, codegenDir, getLog(), classInspection, template);

        visitor.start();

        // if (!f.exists()) {
        // f.mkdirs();
        // }
        //
        // File touch = new File(f, "touch.txt");
        //
        // FileWriter w = null;
        // try {
        // w = new FileWriter(touch);
        //
        // w.write("touch.txt");
        // } catch (IOException e) {
        // throw new MojoExecutionException("Error creating file " + touch, e);
        // } finally {
        // if (w != null) {
        // try {
        // w.close();
        // } catch (IOException e) {
        // // ignore
        // }
        // }
        // }
    }

    private URLClassLoader buildClassloader() throws MojoExecutionException {
        List<URL> urlList = new ArrayList<URL>();
        URL[] urls;
        try {
            File targetClasses = new File(outputDirectory, "classes");
            urlList.add(targetClasses.toURL());

            for (String element : runtimeClasspathElements) {
                getLog().info("Adding " + element);

                try {
                    urlList.add(new File(element).toURI().toURL());
                } catch (MalformedURLException e) {
                    throw new MojoExecutionException("Unable to access project dependency: " + element, e);
                }
            }

            urls = (URL[]) urlList.toArray(new URL[urlList.size()]);
        } catch (MalformedURLException e) {
            throw new MojoExecutionException("Error building URLs", e);
        }

        return new URLClassLoader(urls);
    }

    // private String processClassOld(Class<?> clazz) {
    // StringBuilder sb = new StringBuilder();
    //
    // String className = clazz.getSimpleName();
    //
    // for (Field field : clazz.getFields()) {
    // Class<?> type = field.getType();
    // String typeName = type.getName();
    //
    // String fieldName = field.getName();
    // String beanName = capitalize(fieldName);
    //
    // sb.append("\n");
    // sb.append(typeName + " get" + beanName + "();\n");
    // sb.append("void set" + beanName + "(" + typeName + " value);\n");
    //
    // sb.append("static final Accessor<" + className + ", " + typeName + "> " + beanName);
    // sb.append("= new Accessor<" + className + ", " + typeName + ">() {\n");
    // sb.append("@Override public " + typeName + " get(" + className + " o) {\n");
    // sb.append("return o.get" + beanName + "();\n");
    // sb.append("}\n");
    // sb.append("@Override public void set(" + className + " o, " + typeName + " value) {\n");
    // sb.append("o.set" + beanName + "(value);\n");
    // sb.append("}\n");
    // sb.append("};\n");
    // }
    //
    // return sb.toString();
    // }

}
