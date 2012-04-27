package org.platformlayer.ops.endpoints;

import javax.inject.Inject;
import javax.inject.Provider;

import org.platformlayer.EndpointInfo;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.PublicEndpointBase;
import org.platformlayer.core.model.Tag;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.service.dns.v1.DnsRecord;

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

	@Handler
	public void handler() throws OpsException {
		PublicEndpointBase endpoint = endpointProvider.get();

		if (OpsContext.isConfigure()) {
			// Create a DNS record
			Tag parentTag = OpsSystem.get().createParentTag(endpoint);

			EndpointInfo endpointInfo = EndpointInfo.findEndpoint(endpoint.getTags(), destinationPort);
			if (endpointInfo == null) {
				throw new OpsException("Cannot find endpoint for port: " + destinationPort);
			}

			DnsRecord record = new DnsRecord();
			record.setDnsName(dnsName);
			record.getAddress().add(endpointInfo.publicIp);
			record.getTags().add(parentTag);
			record.key = PlatformLayerKey.fromId(dnsName);
			// endpoint.getId();
			// UniqueKey.build(endpoint, "dns").key;

			try {
				platformLayerClient.putItemByTag(record, parentTag);
			} catch (PlatformLayerClientException e) {
				throw new OpsException("Error registering persistent instance", e);
			}
		}
	}
}
