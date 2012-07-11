package org.platformlayer.service.platformlayer.ops.auth.user;

import java.io.File;

import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.crypto.ManagedSecretKey;
import org.platformlayer.ops.filesystem.ManagedDirectory;
import org.platformlayer.ops.standardservice.StandardServiceInstance;
import org.platformlayer.service.platformlayer.ops.ManagedKeystore;

public class UserAuthInstance extends StandardServiceInstance {

	@Override
	protected UserAuthInstanceModel getTemplate() {
		UserAuthInstanceModel template = injected(UserAuthInstanceModel.class);
		return template;
	}

	@Override
	protected void addChildren() throws OpsException {
		super.addChildren();

		UserAuthInstanceModel template = getTemplate();

		ManagedDirectory configDir = findDirectory(template.getConfigDir());

		// TODO: We don't use services dir any more!
		// File servicesDir = template.getServicesDir();
		// configDir.addChild(ManagedDirectory.build(servicesDir, "0700"));

		// TemplatedFile file = TemplatedFile.build(template, new File(servicesDir, "platformlayer"));
		// configDir.addChild(file);

		ManagedSecretKey sslKey = template.findSslKey();

		ManagedKeystore keystore = configDir.addChild(ManagedKeystore.class);
		keystore.path = new File(configDir.filePath, "../keystore.jks");
		keystore.tagWithPublicKeys = template.getModel();
		keystore.alias = ManagedKeystore.DEFAULT_WEBSERVER_ALIAS;
		keystore.key = sslKey;

	}

}
