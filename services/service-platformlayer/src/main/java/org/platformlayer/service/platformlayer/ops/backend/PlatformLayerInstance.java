package org.platformlayer.service.platformlayer.ops.backend;

import java.io.File;

import org.platformlayer.ops.Bound;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.crypto.ManagedKeystore;
import org.platformlayer.ops.filesystem.ManagedDirectory;
import org.platformlayer.ops.standardservice.StandardServiceInstance;

public class PlatformLayerInstance extends StandardServiceInstance {

	@Bound
	PlatformLayerInstanceModel template;

	@Override
	protected void addChildren() throws OpsException {
		super.addChildren();

		PlatformLayerInstanceModel template = getTemplate();

		ManagedDirectory configDir = findDirectory(template.getConfigDir());
		File keystoreFile = template.getKeystoreFile();

		{
			ManagedKeystore systemAuthKey = configDir.addChild(ManagedKeystore.class);
			systemAuthKey.path = keystoreFile;
			systemAuthKey.tagWithPublicKeys = template.getModel();
			systemAuthKey.alias = template.getSystemCertAlias();
		}

		if (template.isMultitenant()) {
			ManagedKeystore masterProjectKey = configDir.addChild(ManagedKeystore.class);
			masterProjectKey.path = keystoreFile;

			masterProjectKey.alias = template.getMultitenantKeyAlias();
		}
	}

	@Override
	protected PlatformLayerInstanceModel getTemplate() {
		return template;
	}

}
