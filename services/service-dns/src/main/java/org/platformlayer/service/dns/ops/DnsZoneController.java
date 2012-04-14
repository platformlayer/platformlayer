package org.platformlayer.service.dns.ops;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.service.dns.model.DnsZone;

public class DnsZoneController {
	static final Logger log = Logger.getLogger(DnsZoneController.class);

	@Inject
	DnsHelpers dns;

	@Handler
	public void handler(DnsZone dnsDomain) throws OpsException {
		DnsFile dnsFile = dns.buildDnsFile(dnsDomain);

		dns.uploadToAllDnsServers(dnsFile);
	}

}
