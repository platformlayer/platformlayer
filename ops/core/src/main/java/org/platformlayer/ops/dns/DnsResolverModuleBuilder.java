package org.platformlayer.ops.dns;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.packages.AsBlock;
import org.platformlayer.ops.templates.TemplateDataSource;
import org.platformlayer.service.dnsresolver.v1.DnsResolverService;

import com.google.common.collect.Lists;

public class DnsResolverModuleBuilder implements TemplateDataSource {
	static final Logger log = Logger.getLogger(DnsResolverModuleBuilder.class);

	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	InstanceHelpers instances;

	public boolean usePrivateResolvers = true;

	@Override
	public void buildTemplateModel(Map<String, Object> model) throws OpsException {
		List<String> nameservers = Lists.newArrayList();

		Iterable<DnsResolverService> dnsResolverServices = platformLayer.listItems(DnsResolverService.class);
		for (DnsResolverService dnsResolverService : dnsResolverServices) {
			Machine machine = instances.findMachine(dnsResolverService);
			if (machine != null) {
				List<InetAddress> addresses = machine.findAddresses(NetworkPoint.forTargetInContext(), 53);
				for (InetAddress address : addresses) {
					nameservers.add(address.getHostAddress());
				}
			}
		}

		if (nameservers.isEmpty()) {
			log.warn("No (internal) resolvers found; will set up default public nameservers; reconfigure needed if this changes");

			if (usePrivateResolvers) {
				OpsTarget target = OpsContext.get().getInstance(OpsTarget.class);

				AsBlock as = AsBlock.find(target);

				if (as != null) {
					// We had problems with the SL resolvers...
					/*
					 * if (Objects.equal(AsBlock.SOFTLAYER, as)) { log.warn("Adding private Softlayer resolvers");
					 * nameservers.add("10.0.80.11"); nameservers.add("10.0.80.12"); }
					 * 
					 * if (Objects.equal(AsBlock.HETZNER, as)) { log.warn("Adding private Hetzner resolvers");
					 * nameservers.add("213.133.99.99"); nameservers.add("213.133.100.100");
					 * nameservers.add("213.133.98.98"); }
					 */
				}
			}

			nameservers.add("8.8.8.8");
			nameservers.add("8.8.4.4");

			nameservers.add("2001:4860:4860::8888");
			nameservers.add("2001:4860:4860::8844");

			// So a reconfigure is needed!
		}
		model.put("nameservers", nameservers);
	}
}
