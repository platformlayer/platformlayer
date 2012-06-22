package org.platformlayer.service.platformlayer.ops.auth.system;

import org.platformlayer.ops.standardservice.StandardServiceInstance;
import org.platformlayer.ops.standardservice.StandardTemplateData;

public class SystemAuthInstance extends StandardServiceInstance {

	@Override
	protected StandardTemplateData getTemplate() {
		SystemAuthInstanceModel template = injected(SystemAuthInstanceModel.class);
		return template;
	}

}
