package org.platformlayer.ops.dns;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.core.model.EndpointInfo;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.PublicEndpointBase;
import org.platformlayer.core.model.Tag;
import org.platformlayer.dns.model.DnsRecord;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.machines.PlatformLayerHelpers;

public class EndpointDnsRecord {
	public String dnsName;
	public Provider<PublicEndpointBase> endpointProvider;
	public int destinationPort;

	@Inject
	PlatformLayerHelpers platformLayerClient;

	@Inject
	InstanceHelpers instanceHelpers;

	@Inject
	OpsContext opsContext;

	@Inject
	DnsHelpers dns;

	@Handler
	public void handler() throws OpsException {
		PublicEndpointBase endpoint = endpointProvider.get();

		if (OpsContext.isConfigure()) {
			// Create a DNS record
			Tag parentTag = Tag.buildParentTag(endpoint.getKey());

			List<EndpointInfo> endpoints = EndpointInfo.findEndpoints(endpoint.getTags(), destinationPort);
			if (endpoints.isEmpty()) {
				throw new OpsException("Cannot find endpoint for port: " + destinationPort);
			}

			DnsRecord record = new DnsRecord();
			record.setDnsName(dnsName);
			for (EndpointInfo endpointInfo : endpoints) {
				record.getAddress().add(endpointInfo.publicIp);
			}

			record.getTags().add(parentTag);
			record.setKey(PlatformLayerKey.fromId(dnsName));
			// endpoint.getId();
			// UniqueKey.build(endpoint, "dns").key;

			try {
				platformLayerClient.putItemByTag((ItemBase) record, parentTag);
			} catch (PlatformLayerClientException e) {
				throw new OpsException("Error registering persistent instance", e);
			}
		}
	}
}
