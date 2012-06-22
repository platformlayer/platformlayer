package org.platformlayer.service.platformlayer.ops.backend;

import org.platformlayer.ops.standardservice.StandardServiceInstance;

public class PlatformLayerInstance extends StandardServiceInstance {

	@Override
	protected PlatformLayerInstanceModel getTemplate() {
		PlatformLayerInstanceModel template = injected(PlatformLayerInstanceModel.class);
		return template;
	}

}
