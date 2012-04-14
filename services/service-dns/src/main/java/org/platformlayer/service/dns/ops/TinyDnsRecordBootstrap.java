package org.platformlayer.service.dns.ops;

import javax.inject.Inject;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.service.dns.model.DnsRecord;
import org.platformlayer.service.dns.model.DnsZone;

public class TinyDnsRecordBootstrap {
	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	DnsHelpers dns;

	@Handler
	public void handler() throws OpsException {
		Machine machine = OpsContext.get().getInstance(Machine.class);

		// TODO: Only on bootstrap?
		// TODO: What about deleted items?
		for (DnsRecord record : platformLayer.listItems(DnsRecord.class)) {
			DnsFile dnsFile = dns.buildDnsFile(record);

			dns.upload(machine, dnsFile);
		}

		for (DnsZone record : platformLayer.listItems(DnsZone.class)) {
			DnsFile dnsFile = dns.buildDnsFile(record);

			dns.upload(machine, dnsFile);
		}
	}

	public static TinyDnsRecordBootstrap build() {
		return Injection.getInstance(TinyDnsRecordBootstrap.class);
	}
}
