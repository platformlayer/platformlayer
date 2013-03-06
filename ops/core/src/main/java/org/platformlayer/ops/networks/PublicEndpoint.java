package org.platformlayer.ops.networks;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.platformlayer.core.model.EndpointInfo;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.PublicEndpointBase;
import org.platformlayer.core.model.TagChanges;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsProvider;
import org.platformlayer.ops.dns.EndpointDnsRecord;
import org.platformlayer.ops.firewall.Protocol;
import org.platformlayer.ops.firewall.Transport;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.machines.PlatformLayerCloudHelpers;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.tagger.Tagger;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.TimeSpan;
import com.google.common.base.Strings;

public class PublicEndpoint extends OpsTreeBase {
	static final Logger log = LoggerFactory.getLogger(PublicEndpoint.class);

	// public String network;
	public int publicPort;
	public int backendPort;
	// public ItemBase item;
	public String dnsName;
	public PlatformLayerKey tagItem;
	public PlatformLayerKey parentItem;

	public List<Integer> publicPortCluster;

	public boolean defaultBlocked = true;

	public Protocol protocol = Protocol.Tcp;
	public Transport transport = null;

	@Inject
	PlatformLayerHelpers platformLayerClient;

	@Inject
	PlatformLayerCloudHelpers platformLayerCloudHelpers;

	@Inject
	InstanceHelpers instanceHelpers;

	@Inject
	OpsContext opsContext;

	@Handler
	public void handler() {

	}

	@Override
	protected void addChildren() throws OpsException {
		final OwnedEndpoint endpoint;

		{
			endpoint = addChild(OwnedEndpoint.class);
			endpoint.publicPort = publicPort;
			endpoint.publicPortCluster = publicPortCluster;
			endpoint.backendPort = backendPort;
			endpoint.parentItem = parentItem;
			endpoint.transport = transport;
		}

		if (tagItem != null) {
			Tagger tagger = injected(Tagger.class);

			OpsProvider<TagChanges> tagChanges = new OpsProvider<TagChanges>() {
				@Override
				public TagChanges get() throws OpsException {
					int maxAttempts = 5;
					int attempt = 0;

					PublicEndpointBase item = endpoint.getItem();

					while (true) {
						attempt++;

						if (item == null) {
							if (!OpsContext.isDelete()) {
								throw new OpsException("Endpoint not created");
							} else {
								log.warn("No endpoint => no tagging to be done");
								return null;
							}
						}

						List<EndpointInfo> endpointInfos = EndpointInfo.findEndpoints(item.getTags(), publicPort);
						if (!endpointInfos.isEmpty()) {
							TagChanges tagChanges = new TagChanges();
							for (EndpointInfo endpointInfo : endpointInfos) {
								tagChanges.addTags.add(endpointInfo.toTag());
							}

							return tagChanges;
						}

						if (attempt != maxAttempts) {
							log.info("Endpoint not yet found; sleeping and retrying");
							TimeSpan.FIVE_SECONDS.doSafeSleep();

							item = platformLayerClient.getItem(item.getKey());
							continue;
						} else {
							throw new OpsException("Cannot find endpoint for port: " + publicPort);
						}
					}
				}
			};
			tagger.platformLayerKey = tagItem;
			tagger.tagChangesProvider = tagChanges;

			addChild(tagger);
		}

		if (defaultBlocked) {
			// Block on machine's firewall
			log.warn("Not adding firewall block; relying on default block");
			// addChild(IptablesFirewallEntry.build(FirewallRecord.buildBlockPort(protocol, backendPort)));
		}

		if (!Strings.isNullOrEmpty(dnsName)) {
			EndpointDnsRecord dns = injected(EndpointDnsRecord.class);
			dns.destinationPort = publicPort;
			dns.endpointProvider = new Provider<PublicEndpointBase>() {
				@Override
				public PublicEndpointBase get() {
					return endpoint.getItem();
				}
			};

			dns.dnsName = dnsName;
			addChild(dns);
		}
	}
}
