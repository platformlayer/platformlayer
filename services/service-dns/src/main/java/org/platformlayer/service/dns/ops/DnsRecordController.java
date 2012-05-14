package org.platformlayer.service.dns.ops;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.service.dns.model.DnsRecord;

public class DnsRecordController {
	static final Logger log = Logger.getLogger(DnsRecordController.class);

	@Inject
	DnsHelpers dns;

	@Handler
	public void handler(DnsRecord dnsRecord) throws OpsException {
		ZoneFile dnsFile = dns.buildDnsFile(dnsRecord);

		dns.uploadToAllDnsServers(dnsFile);
	}

}
