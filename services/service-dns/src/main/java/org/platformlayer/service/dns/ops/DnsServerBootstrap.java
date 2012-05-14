package org.platformlayer.service.dns.ops;

import javax.inject.Inject;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.service.dns.model.DnsZone;

public class DnsServerBootstrap {
	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	DnsHelpers dns;

	@Handler
	public void handler(OpsTarget target) throws OpsException {
		for (DnsZone record : platformLayer.listItems(DnsZone.class)) {
			ZoneFile dnsFile = dns.buildDnsFile(record);

			dns.upload(target, dnsFile);
		}
	}
}
