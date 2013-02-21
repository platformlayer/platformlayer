package org.platformlayer.service.platformlayer.ops.auth.system;

import org.platformlayer.ops.Bound;
import org.platformlayer.ops.standardservice.StandardServiceInstance;

public class SystemAuthInstance extends StandardServiceInstance {

	@Bound
	SystemAuthInstanceModel template;

	@Override
	protected SystemAuthInstanceModel getTemplate() {
		return template;
	}

}
