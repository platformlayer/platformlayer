package org.platformlayer.service.httpfrontend.ops;

import java.io.File;

import org.platformlayer.ops.Bound;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.ManagedDirectory;
import org.platformlayer.ops.filesystem.TemplatedFile;
import org.platformlayer.ops.standardservice.StandardServiceInstance;

public class HttpServerInstance extends StandardServiceInstance {
	@Bound
	HttpServerTemplateData template;

	@Override
	protected HttpServerTemplateData getTemplate() {
		return template;
	}

	@Override
	protected void addChildren() throws OpsException {
		super.addChildren();

		addChild(TemplatedFile.build(template, template.getLogConfigurationFile()));

		// TODO: Ownership
		addChild(ManagedDirectory.build(template.getHostsDir(), "755"));
		addChild(ManagedDirectory.build(template.getCacheDir(), "755"));
		addChild(ManagedDirectory.build(template.getLogsDir(), "755"));

		File sslBaseDir = template.getSslBaseDir();
		addChild(ManagedDirectory.build(sslBaseDir, "755"));
		addChild(ManagedDirectory.build(new File(sslBaseDir, "config"), "755"));
		addChild(ManagedDirectory.build(new File(sslBaseDir, "keystore"), "755"));

		// TODO: Reintroduce this
		// We split the configuration because we want to configure the services before we bring up DNS
		addChild(HttpSiteConfiguration.class);

		// {
		// StandardService service = addChild(StandardService.class);
		// service.key = "httpservice";
		// service.instanceDir = template.getInstanceDir();
		// service.user = template.getUser();
		// service.setCommand(template.getCommand());
		// service.environment = Providers.of(template.getEnvironment());
		// }

		addChild(HttpDnsConfiguration.class);
	}

	// @Inject
	// PlatformLayerHelpers platformLayer;
	//
	// @Handler
	// public void handler() {
	// }
	//
	// @Override
	// protected void addChildren() throws OpsException {
	// HttpServerTemplateData template = injected(HttpServerTemplateData.class);
	//
	// File instanceDir = template.getInstanceDir();
	//
	// addChild(ManagedDirectory.build(instanceDir, "0755"));
	//
	// addChild(TemplatedFile.build(template, template.getLogConfigurationFile()));
	//
	// addChild(ManagedDirectory.build(template.getHostsDir(), "755"));
	//
	// // We split the configuration because we want to configure the services before we bring up DNS
	// addChild(HttpSiteConfiguration.class);
	//
	// {
	// StandardService service = addChild(StandardService.class);
	// service.key = "httpservice";
	// service.instanceDir = template.getInstanceDir();
	// service.user = template.getUser();
	// service.setCommand(template.getCommand());
	// service.environment = Providers.of(template.getEnvironment());
	// }
	//
	// addChild(HttpDnsConfiguration.class);
	// }
}
