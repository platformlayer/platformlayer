package org.platformlayer.service.platformlayer.ops.auth.system;

import org.platformlayer.ops.Bound;
import org.platformlayer.ops.standardservice.StandardServiceInstall;
import org.platformlayer.ops.standardservice.StandardTemplateData;

public class SystemAuthInstall extends StandardServiceInstall {

	@Bound
	SystemAuthInstanceTemplate template;

	@Override
	protected StandardTemplateData getTemplate() {
		return template;
	}

}
