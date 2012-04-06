package org.platformlayer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import freemarker.cache.URLTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

public class TemplateEngine {

    Configuration cfg;
    private final ClassLoader classLoader;
    private final Log log;

    class ResourceTemplateLoader extends URLTemplateLoader {
        String prefix = "";

        public ResourceTemplateLoader() {
        }

        protected URL getURL(String name) {
            URL url = classLoader.getResource(prefix + name);
            // if (url == null && baseDir != null) {
            // File file = new File(baseDir, name);
            //
            // if (file.exists()) {
            // try {
            // url = file.toURI().toURL();
            // } catch (MalformedURLException e) {
            // log.warn("Cannot build URL for file: " + file, e);
            // }
            // }
            // }

            if (url == null) {
                log.warn("Unable to find resource: " + name + " in ClassLoader: " + classLoader);
            }
            return url;
        }
    }

    public TemplateEngine(ClassLoader classLoader, Log log) {
        this.classLoader = classLoader;
        this.log = log;

        cfg = new Configuration();

        // File baseDir = new File(System.getProperty("user.dir"));
        // cfg.setTemplateLoader(new MyTemplateLoader(baseDir));

        cfg.setTemplateLoader(new ResourceTemplateLoader());

        // Don't put commas into numbers!!
        cfg.setNumberFormat("0.############");

        // try {
        // cfg.setDirectoryForTemplateLoading(AppConfiguration.INSTANCE.getAppDirectory());
        // } catch (IOException e) {
        // throw new RuntimeException("Failed to set freemarker load directory", e);
        // }

        // Specify how templates will see the data-model. This is an advanced
        // topic...
        // but just use this:

        DefaultObjectWrapper objectWrapper = new DefaultObjectWrapper();
        objectWrapper.setExposeFields(true);
        cfg.setObjectWrapper(objectWrapper);
    }

    private Template getTemplate(String templateName) throws IOException {
        // Configuration does auto-caching of templates
        try {
            Template template = cfg.getTemplate(templateName);
            return template;
        } catch (FileNotFoundException fnf) {
            throw new IOException("Template not found: " + templateName);
        }
    }

    public void runTemplate(String templateName, Map<String, Object> model, Writer writer) throws MojoExecutionException, IOException {
        Template template;
        try {
            template = getTemplate(templateName);
        } catch (IOException e) {
            throw new MojoExecutionException("Error reading template: " + templateName, e);
        }

        try {
            template.process(model, writer);
        } catch (freemarker.template.TemplateException e) {
            throw new MojoExecutionException("Error running template: " + templateName, e);
        }

        writer.flush();
    }

    public String runTemplateToString(String templateName, Map<String, Object> model) throws MojoExecutionException {
        StringWriter writer = new StringWriter();
        try {
            runTemplate(templateName, model, writer);
        } catch (IOException e) {
            // This shouldn't happen to a stringwriter...
            throw new MojoExecutionException("IOException running template: " + templateName, e);
        }
        return writer.toString();
    }

}
