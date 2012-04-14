package org.platformlayer.service.dns.ops;

import java.io.File;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.TimeSpan;
import org.platformlayer.ops.CloudContext;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.endpoints.EndpointHelpers;
import org.platformlayer.ops.endpoints.EndpointInfo;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.helpers.ServiceContext;
import org.platformlayer.ops.helpers.SshKey;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.service.dns.model.DnsRecord;
import org.platformlayer.service.dns.model.DnsServer;
import org.platformlayer.service.dns.model.DnsZone;

public class DnsHelpers {

	@Inject
	CloudContext cloud;

	@Inject
	ServiceContext service;

	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	InstanceHelpers instances;

	@Inject
	EndpointHelpers endpointHelpers;

	static final Logger log = Logger.getLogger(DnsHelpers.class);

	public void upload(Machine machine, DnsFile dnsFile) throws OpsException {
		File recordsRoot = new File("/var/dns/records/");
		// TODO: Validate / sanitize dnsName
		File recordsFile = new File(recordsRoot, dnsFile.getKey());
		File signalFile = new File("/var/dns/dirty");

		SshKey sshKey = service.getSshKey();
		OpsTarget target = machine.getTarget(sshKey);
		target.setFileContents(recordsFile, dnsFile.getData());

		target.touchFile(signalFile);
	}

	public DnsFile buildDnsFile(DnsZone dnsDomain) throws OpsException {
		String key = "domain_" + dnsDomain.dnsName;
		DnsFile dnsFile = new DnsFile(key);

		Iterable<DnsServer> dnsServers = platformLayer.listItems(DnsServer.class);

		// We do two passes; first to collect up all the nameservers, and the second to configure the domain with the
		// list of nameservers
		for (DnsServer dnsServer : dnsServers) {
			switch (dnsServer.state) {
			case DELETE_REQUESTED:
			case DELETED:
				log.info("Skipping server (deleted/deleting): " + dnsServer);
				continue;
			}
			EndpointInfo dnsServerEndpoint = endpointHelpers.findEndpoint(dnsServer.getTags(), 53);

			if (dnsServerEndpoint == null) {
				throw new OpsException("Cannot find endpoint for: " + dnsServer);
			}
			// Use the ID to produce a stable identifier
			// TODO: What if we shutdown nameservers? Should we do something like consistent hashing instead?
			// i.e. always create ns1 ... ns16, and then dynamically repoint them as we add/remove nameservers?
			// Does this really help?
			String serverId = "ns" + dnsServer.getId();

			// TODO: This might not be the right address in complex networks
			String address = dnsServerEndpoint.publicIp;
			dnsFile.addNS(dnsDomain.dnsName, address, serverId);
		}

		return dnsFile;
	}

	public DnsFile buildDnsFile(DnsRecord dnsRecord) {
		String key = "record_" + dnsRecord.dnsName;
		DnsFile dnsFile = new DnsFile(key);
		dnsFile.addA(dnsRecord.dnsName, dnsRecord.address);

		return dnsFile;
	}

	public void uploadToAllDnsServers(DnsFile dnsFile) throws OpsException {
		// TODO: Likely problems with concurrent creation of servers and domain entries??

		boolean failed = false;

		Iterable<DnsServer> dnsServers = platformLayer.listItems(DnsServer.class);
		for (DnsServer dnsServer : dnsServers) {
			switch (dnsServer.state) {
			case DELETE_REQUESTED:
			case DELETED:
				log.info("Skipping server (deleted/deleting): " + dnsServer);
				continue;
			case ACTIVE:
				// Good
				break;
			default:
				log.warn("Dns server not yet active: " + dnsServer);
				failed = true;
				continue;
			}

			Machine machine = instances.findMachine(dnsServer);
			if (machine == null) {
				log.warn("Dns server instance not found: " + dnsServer);
				failed = true;
				continue;
			}

			// TODO: We could have a 'cluster key' that is used to tag the
			// machines providing this service

			upload(machine, dnsFile);
		}

		if (failed) {
			throw new OpsException("Could not update all DNS servers in cluster").setRetry(TimeSpan.ONE_MINUTE);
		}
	}
}
