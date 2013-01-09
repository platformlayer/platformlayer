package org.platformlayer.service.dns.ops;

import javax.inject.Inject;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.service.dns.model.DnsZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DnsZoneController {

	private static final Logger log = LoggerFactory.getLogger(DnsZoneController.class);

	@Inject
	DnsHelpers dns;

	@Handler
	public void handler(DnsZone dnsDomain) throws OpsException {
		ZoneFile dnsFile = dns.buildDnsFile(dnsDomain);

		dns.uploadToAllDnsServers(dnsFile);
	}

}
