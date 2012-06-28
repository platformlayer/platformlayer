package org.platformlayer.service.platformlayer.ops.auth.system;

import org.platformlayer.ops.standardservice.StandardServiceInstance;

public class SystemAuthInstance extends StandardServiceInstance {

	@Override
	protected SystemAuthInstanceModel getTemplate() {
		SystemAuthInstanceModel template = injected(SystemAuthInstanceModel.class);
		return template;
	}

}
