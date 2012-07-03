package org.platformlayer.service.platformlayer.ops.auth.system;

import java.io.File;

import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.ManagedDirectory;
import org.platformlayer.ops.standardservice.StandardServiceInstance;

public class SystemAuthInstance extends StandardServiceInstance {

	@Override
	protected void addChildren() throws OpsException {
		super.addChildren();

		SystemAuthInstanceModel template = getTemplate();

		ManagedDirectory configDir = findDirectory(template.getConfigDir());

		ManagedKeystore keystore = configDir.addChild(ManagedKeystore.class);
		keystore.path = new File(configDir.filePath, "../keystore.jks");
	}

	@Override
	protected SystemAuthInstanceModel getTemplate() {
		SystemAuthInstanceModel template = injected(SystemAuthInstanceModel.class);
		return template;
	}

}
