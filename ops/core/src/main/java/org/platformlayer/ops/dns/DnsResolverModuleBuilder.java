package org.platformlayer.ops.dns;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.helpers.ProviderHelper;
import org.platformlayer.ops.helpers.ProviderHelper.ProviderOf;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.packages.AsBlock;
import org.platformlayer.ops.templates.TemplateDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class DnsResolverModuleBuilder implements TemplateDataSource {
	static final Logger log = LoggerFactory.getLogger(DnsResolverModuleBuilder.class);

	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	InstanceHelpers instances;

	@Inject
	ProviderHelper providers;

	public boolean usePrivateResolvers = true;

	@Override
	public void buildTemplateModel(Map<String, Object> model) throws OpsException {
		List<String> nameservers = Lists.newArrayList();

		for (ProviderOf<DnsResolverProvider> entry : providers.listItemsProviding(DnsResolverProvider.class)) {
			DnsResolverProvider dnsResolverProvider = entry.get();
			List<InetAddress> addresses = dnsResolverProvider.findAddresses(NetworkPoint.forTargetInContext());

			for (InetAddress address : addresses) {
				nameservers.add(address.getHostAddress());
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
