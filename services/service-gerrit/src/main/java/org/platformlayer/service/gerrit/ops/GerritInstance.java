package org.platformlayer.service.gerrit.ops;

import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.standardservice.StandardServiceInstance;
import org.platformlayer.ops.standardservice.StandardTemplateData;

public class GerritInstance extends StandardServiceInstance {

	@Override
	protected GerritInstanceModel getTemplate() {
		GerritInstanceModel template = injected(GerritInstanceModel.class);
		return template;
	}

	@Override
	protected void addConfigurationFile(final StandardTemplateData template) throws OpsException {
		// Bootstrap depends on configuration file, so we build this much earlier...
	}

}
