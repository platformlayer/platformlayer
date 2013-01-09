package org.platformlayer.service.dnsresolver.ops;

import javax.inject.Inject;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.helpers.ImageFactory;
import org.platformlayer.ops.instances.DiskImageRecipeBuilder;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.metrics.MetricsInstance;
import org.platformlayer.ops.packages.PackageDependency;
import org.platformlayer.ops.service.ManagedService;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.dnsresolver.model.DnsResolverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class DnsResolverServiceController extends OpsTreeBase {
	private static final Logger log = LoggerFactory.getLogger(DnsResolverServiceController.class);

	@Inject
	ImageFactory imageFactory;

	@Handler
	public void doOperation() {
	}

	@Override
	protected void addChildren() throws OpsException {
		DnsResolverService model = OpsContext.get().getInstance(DnsResolverService.class);
		if (Strings.isNullOrEmpty(model.dnsName)) {
			throw new IllegalArgumentException("dnsName must be specified");
		}

		InstanceBuilder instance = InstanceBuilder.build(model.dnsName,
				DiskImageRecipeBuilder.buildDiskImageRecipe(this));
		addChild(instance);

		instance.addChild(PackageDependency.build("bind9"));
		instance.addChild(ManagedService.build("bind9"));

		instance.addChild(MetricsInstance.class);

		// Debian bind9 sets up a recursive resolver by default :-)

		// TODO: Monit

		// TODO: Configure /etc/resolv.conf on servers
		// TODO: Refresh all our servers so that they use this resolver??

	}
}
