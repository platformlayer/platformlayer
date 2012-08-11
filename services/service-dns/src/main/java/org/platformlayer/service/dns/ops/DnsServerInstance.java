package org.platformlayer.service.dns.ops;

import org.platformlayer.ops.Bound;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.ManagedDirectory;
import org.platformlayer.ops.filesystem.TemplatedFile;
import org.platformlayer.ops.standardservice.StandardServiceInstance;

public class DnsServerInstance extends StandardServiceInstance {

	@Bound
	DnsServerTemplate template;

	@Override
	protected DnsServerTemplate getTemplate() {
		return template;
	}

	@Override
	protected void addExtraFiles() throws OpsException {
		addChild(TemplatedFile.build(template, template.getLogConfigurationPath()).setFileMode("0444"));

		addChild(ManagedDirectory.build(DnsServerTemplate.getZonesDir(), "755"));

		addChild(DnsServerBootstrap.class);
	}

}
