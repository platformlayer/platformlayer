package org.platformlayer.service.httpfrontend.ops;

import java.io.File;

import javax.inject.Inject;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.ManagedDirectory;
import org.platformlayer.ops.filesystem.TemplatedFile;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.supervisor.StandardService;
import org.platformlayer.ops.tree.OpsTreeBase;

public class HttpServerInstance extends OpsTreeBase {
	@Inject
	PlatformLayerHelpers platformLayer;

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		HttpServerTemplateData template = injected(HttpServerTemplateData.class);

		File instanceDir = template.getInstanceDir();

		addChild(ManagedDirectory.build(instanceDir, "0755"));

		addChild(TemplatedFile.build(template, template.getLogConfigurationFile()));

		addChild(ManagedDirectory.build(template.getHostsDir(), "755"));

		// We split the configuration because we want to configure the services before we bring up DNS
		addChild(HttpSiteConfiguration.class);

		{
			StandardService service = addChild(StandardService.class);
			service.key = "httpservice";
			service.instanceDir = template.getInstanceDir();
			service.user = template.getUser();
			service.setCommand(template.getCommand());
		}

		addChild(HttpDnsConfiguration.class);
	}
}
