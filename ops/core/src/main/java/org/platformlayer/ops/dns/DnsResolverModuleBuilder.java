package org.platformlayer.ops.dns;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.templates.TemplateDataSource;
import org.platformlayer.service.dnsresolver.v1.DnsResolverService;

import com.google.common.collect.Lists;

public class DnsResolverModuleBuilder implements TemplateDataSource {
	static final Logger log = Logger.getLogger(DnsResolverModuleBuilder.class);

	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	InstanceHelpers instances;

	@Override
	public void buildTemplateModel(Map<String, Object> model) throws OpsException {
		List<String> nameservers = Lists.newArrayList();

		Iterable<DnsResolverService> dnsResolverServices = platformLayer.listItems(DnsResolverService.class);
		for (DnsResolverService dnsResolverService : dnsResolverServices) {
			Machine machine = instances.findMachine(dnsResolverService);
			if (machine != null) {
				String address = machine.findAddress(NetworkPoint.forTargetInContext(), 53);
				if (address != null) {
					nameservers.add(address);
				}
			}
		}

		if (nameservers.isEmpty()) {
			log.warn("No (internal) resolvers found; will set up default public nameservers; reconfigure needed");
			nameservers.add("8.8.8.8");
			nameservers.add("8.8.4.4");

			// So a reconfigure is needed!
		}
		model.put("nameservers", nameservers);
	}

}
