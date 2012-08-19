package org.platformlayer.service.cloud.direct.ops.kvm.host;

import java.io.File;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.SimpleFile;
import org.platformlayer.ops.packages.PackageDependency;
import org.platformlayer.ops.service.ManagedService;
import org.platformlayer.ops.supervisor.SupervisordInstall;
import org.platformlayer.ops.tree.OpsTreeBase;

public class KvmHost extends OpsTreeBase {

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		addChild(injected(SupervisordInstall.class));

		// To make config cd
		addChild(PackageDependency.build("genisoimage"));

		addChild(PackageDependency.build("kvm"));

		// For tunctl
		addChild(PackageDependency.build("uml-utilities"));

		addChild(SimpleFile.build(getClass(), new File("/etc/default/uml-utilities")));
		addChild(ManagedService.build("uml-utilities").setEnabled(false));
	}
}
