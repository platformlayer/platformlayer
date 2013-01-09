package org.platformlayer.ops.templates;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.slf4j.*;

import com.google.inject.Singleton;

import freemarker.cache.URLTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;

@Singleton
public class FreemarkerTemplateEngine implements TemplateEngine {
	static final Logger log = LoggerFactory.getLogger(FreemarkerTemplateEngine.class);

	Configuration cfg;

	class MyTemplateLoader extends URLTemplateLoader {
		final File baseDir;

		public MyTemplateLoader(File baseDir) {
			this.baseDir = baseDir;

			classLoader = FreemarkerTemplateEngine.class.getClassLoader();
		}

		ClassLoader classLoader;
		String prefix = "";

		@Override
		protected URL getURL(String name) {
			URL url = classLoader.getResource(prefix + name);
			if (url == null && baseDir != null) {
				File file = new File(baseDir, name);

				if (file.exists()) {
					try {
						url = file.toURI().toURL();
					} catch (MalformedURLException e) {
						log.warn("Cannot build URL for file: " + file, e);
					}
				}
			}
			return url;
		}
	}

	public FreemarkerTemplateEngine() {
		cfg = new Configuration();

		File baseDir = new File(System.getProperty("user.dir"));
		cfg.setTemplateLoader(new MyTemplateLoader(baseDir));

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
		PlatformLayerObjectWrapper objectWrapper = new PlatformLayerObjectWrapper();
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

	@Override
	public void runTemplate(String templateName, Map<String, Object> model, Writer writer) throws TemplateException,
			IOException {
		Template template;
		try {
			template = getTemplate(templateName);
		} catch (IOException e) {
			throw new TemplateException("Error reading template: " + templateName, e);
		}

		try {
			template.process(model, writer);
		} catch (freemarker.template.TemplateException e) {
			throw new TemplateException("Error running template: " + templateName, e);
		}

		writer.flush();
	}

	@Override
	public String runTemplateToString(String templateName, Map<String, Object> model) throws TemplateException {
		StringWriter writer = new StringWriter();
		try {
			runTemplate(templateName, model, writer);
		} catch (IOException e) {
			// This shouldn't happen to a stringwriter...
			throw new TemplateException("IOException running template: " + templateName, e);
		}
		return writer.toString();
	}
}
