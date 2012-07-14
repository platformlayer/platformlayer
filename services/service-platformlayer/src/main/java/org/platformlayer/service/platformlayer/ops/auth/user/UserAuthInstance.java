package org.platformlayer.service.platformlayer.ops.auth.user;

import org.platformlayer.ops.standardservice.StandardServiceInstance;

public class UserAuthInstance extends StandardServiceInstance {

	@Override
	protected UserAuthInstanceModel getTemplate() {
		UserAuthInstanceModel template = injected(UserAuthInstanceModel.class);
		return template;
	}

}
