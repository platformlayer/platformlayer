package org.platformlayer.service.platformlayer.ops.backend;

import java.io.File;

import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.ManagedDirectory;
import org.platformlayer.ops.standardservice.StandardServiceInstance;
import org.platformlayer.service.platformlayer.ops.ManagedKeystore;

public class PlatformLayerInstance extends StandardServiceInstance {

	@Override
	protected void addChildren() throws OpsException {
		super.addChildren();

		PlatformLayerInstanceModel template = getTemplate();

		ManagedDirectory configDir = findDirectory(template.getConfigDir());

		ManagedKeystore keystore = configDir.addChild(ManagedKeystore.class);
		keystore.path = new File(configDir.filePath, "../keystore.jks");
		keystore.tagWithPublicKeys = template.getModel();
	}

	@Override
	protected PlatformLayerInstanceModel getTemplate() {
		PlatformLayerInstanceModel template = injected(PlatformLayerInstanceModel.class);
		return template;
	}

}
