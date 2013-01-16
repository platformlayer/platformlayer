package org.platformlayer.service.dns.ops;

import javax.inject.Inject;

import org.platformlayer.dns.model.DnsRecord;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DnsRecordController {

	private static final Logger log = LoggerFactory.getLogger(DnsRecordController.class);

	@Inject
	DnsHelpers dns;

	@Handler
	public void handler(DnsRecord dnsRecord) throws OpsException {
		ZoneFile dnsFile = dns.buildDnsFile(dnsRecord);

		dns.uploadToAllDnsServers(dnsFile);
	}

}
