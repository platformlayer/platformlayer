package org.platformlayer.ops.helpers;

import java.util.Map;

import javax.inject.Inject;

import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.templates.TemplateEngine;
import org.platformlayer.ops.templates.TemplateException;

public class TemplateHelpers {
    @Inject
    TemplateEngine templateEngine;

    public String runTemplate(String resourceName, Map<String, Object> model) throws OpsException {
        String templateName = resourceName;
        try {
            return templateEngine.runTemplateToString(templateName, model);
        } catch (TemplateException e) {
            throw new OpsException("Error running template", e);
        }
    }

    public String toResourcePath(Object base, String relativePath) {
        String basePath = base.getClass().getPackage().getName().replace(".", "/");
        basePath += "/";
        return basePath + relativePath;
    }
}
