package org.platformlayer.service.wordpress.ops;

import java.io.File;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.ManagedSymlink;
import org.platformlayer.ops.tree.OpsTreeBase;

public class ApacheSite extends OpsTreeBase {
	String siteName;

	@Handler
	public void handler() throws OpsException {
	}

	public static ApacheSite build(String siteName) {
		ApacheSite apacheModule = Injection.getInstance(ApacheSite.class);
		apacheModule.siteName = siteName;
		return apacheModule;
	}

	@Override
	protected void addChildren() throws OpsException {
		File apache2ConfDir = new File("/etc/apache2");
		File sitesAvailableDir = new File(apache2ConfDir, "sites-available");
		File sitesEnabledDir = new File(apache2ConfDir, "sites-enabled");

		File symlinkAvailable = new File(sitesAvailableDir, siteName);
		File symlinkEnabled = new File(sitesEnabledDir, siteName);

		addChild(ManagedSymlink.build(symlinkEnabled, symlinkAvailable));
	}

}
