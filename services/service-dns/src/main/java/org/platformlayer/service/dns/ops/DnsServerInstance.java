package org.platformlayer.service.dns.ops;

import java.io.File;

import javax.inject.Inject;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.ManagedDirectory;
import org.platformlayer.ops.filesystem.ManagedSymlink;
import org.platformlayer.ops.filesystem.TemplatedFile;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.supervisor.SupervisorInstance;
import org.platformlayer.ops.tree.OpsTreeBase;

public class DnsServerInstance extends OpsTreeBase {
	@Inject
	PlatformLayerHelpers platformLayer;

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		DnsServerTemplateData template = injected(DnsServerTemplateData.class);

		File instanceDir = template.getInstanceDir();

		addChild(ManagedDirectory.build(instanceDir, "0755"));

		addChild(TemplatedFile.build(template, new File(instanceDir, "supervisor.conf")).setFileMode("0444"));
		addChild(TemplatedFile.build(template, new File(instanceDir, "logback.xml")).setFileMode("0444"));

		addChild(ManagedDirectory.build(template.getZonesDir(), "755"));

		addChild(DnsServerBootstrap.class);

		String supervisorKey = "dns";

		{
			ManagedSymlink symlink = ManagedSymlink.build(
					new File("/etc/supervisor/conf.d/" + supervisorKey + ".conf"), new File(instanceDir,
							"supervisor.conf"));
			addChild(symlink);
		}

		{
			SupervisorInstance service = injected(SupervisorInstance.class);
			service.id = supervisorKey;
			addChild(service);
		}
	}
}
