package org.platformlayer.service.platformlayer.ops.auth.user;

import java.io.File;

import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.ManagedDirectory;
import org.platformlayer.ops.filesystem.TemplatedFile;
import org.platformlayer.ops.standardservice.StandardServiceInstance;

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

		File servicesDir = template.getServicesDir();
		configDir.addChild(ManagedDirectory.build(servicesDir, "0700"));

		TemplatedFile file = TemplatedFile.build(template, new File(servicesDir, "platformlayer"));
		configDir.addChild(file);
	}

	protected ManagedDirectory findDirectory(File dir) throws OpsException {
		for (ManagedDirectory o : getChildren(ManagedDirectory.class)) {
			if (o.filePath.equals(dir)) {
				return o;
			}
		}
		return null;
	}

}
