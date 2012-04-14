package org.platformlayer.ops.templates;

import java.util.Map;

import org.platformlayer.ops.OpsException;

public interface TemplateDataSource {
	void buildTemplateModel(Map<String, Object> model) throws OpsException;
}
