package org.platformlayer.service.platformlayer.ops.auth.user;

import org.platformlayer.ops.Bound;
import org.platformlayer.ops.standardservice.StandardServiceInstance;

public class UserAuthInstance extends StandardServiceInstance {

	@Bound
	UserAuthInstanceTemplate template;

	@Override
	protected UserAuthInstanceTemplate getTemplate() {
		return template;
	}

}
