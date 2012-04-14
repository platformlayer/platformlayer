package org.platformlayer.ops.templates;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import com.google.inject.ImplementedBy;

@ImplementedBy(FreemarkerTemplateEngine.class)
public interface TemplateEngine {
	String runTemplateToString(String templateName, Map<String, Object> model) throws TemplateException;

	void runTemplate(String templateName, Map<String, Object> model, Writer writer) throws TemplateException,
			IOException;
}
