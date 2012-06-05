package org.platformlayer.service.dns.ops;

import java.io.File;
import java.util.List;

import javalang7.AutoCloseable;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.TimeSpan;
import org.platformlayer.core.model.EndpointInfo;
import org.platformlayer.ops.CloudContext;
import org.platformlayer.ops.FileUpload;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.helpers.ServiceContext;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.service.dns.model.DnsRecord;
import org.platformlayer.service.dns.model.DnsServer;
import org.platformlayer.service.dns.model.DnsZone;

import com.google.common.base.Objects;

public class DnsHelpers {
	static final Logger log = Logger.getLogger(DnsHelpers.class);

	@Inject
	CloudContext cloud;

	@Inject
	ServiceContext service;

	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	InstanceHelpers instances;

	public static final File PATH_BASE = new File("/var/dns");
	public static final File PATH_ZONES = new File(PATH_BASE, "zones");

	public void upload(OpsTarget target, ZoneFile zoneFile) throws OpsException {
		upload(new TargetServer(target), zoneFile);
	}

	public void upload(TargetServer targetServer, ZoneFile zoneFile) throws OpsException {
		// TODO: Validate / sanitize key
		File path = new File(PATH_ZONES, zoneFile.getKey());
		String data = zoneFile.getData();

		OpsTarget target = targetServer.getTarget();

		String existing = target.readTextFile(path);
		boolean isSame = Objects.equal(data, existing);

		if (!isSame) {
			// TODO: The serial value means that this is always dirty
			log.info("Uploading zone file: " + path);

			// Upload then atomic move
			File tempFile = new File(targetServer.getTempDir(), zoneFile.getKey());

			FileUpload upload = FileUpload.build(data);
			upload.path = tempFile;
			upload.mode = "0644";

			target.doUpload(upload);

			target.mv(tempFile, path);
		}
	}

	static String toZone(DnsRecord record) {
		String dnsName = record.dnsName;
		int firstDot = dnsName.indexOf('.');
		if (firstDot != -1) {
			return dnsName.substring(firstDot + 1);
		}

		throw new IllegalArgumentException("Unexpected name: " + dnsName);
	}

	public ZoneFile buildDnsFile(DnsZone dnsDomain) throws OpsException {
		ZoneFile dnsFile = new ZoneFile(dnsDomain.dnsName);

		String dnsZone = dnsDomain.dnsName;

		Iterable<DnsServer> dnsServers = platformLayer.listItems(DnsServer.class);
		// We do two passes; first to collect up all the nameservers, and the second to configure the domain with the
		// list of nameservers

		for (DnsServer dnsServer : dnsServers) {
			switch (dnsServer.getState()) {
			case DELETE_REQUESTED:
			case DELETED:
				log.info("Skipping server (deleted/deleting): " + dnsServer);
				continue;
			case ACTIVE:
				// Good
				break;
			default:
				log.warn("Dns server not yet active: " + dnsServer);
				// failed = true;
				continue;
			}

			List<EndpointInfo> dnsServerEndpoints = EndpointInfo.findEndpoints(dnsServer.getTags(), 53);
			if (dnsServerEndpoints.isEmpty()) {
				throw new OpsException("Cannot find endpoint for: " + dnsServer);
			}
			// Use the ID to produce a stable identifier
			// TODO: What if we shutdown nameservers? Should we do something like consistent hashing instead?
			// i.e. always create ns1 ... ns16, and then dynamically repoint them as we add/remove nameservers?
			// Does this really help?
			// String serverId = dnsServer"ns" + dnsServer.getId();
			String dnsName = dnsServer.dnsName;
			if (dnsName == null) {
				throw new OpsException("DnsName not set on " + dnsServer);
			}

			// TODO: This might not be the right address in complex networks
			for (EndpointInfo dnsServerEndpoint : dnsServerEndpoints) {
				String address = dnsServerEndpoint.publicIp;
				dnsFile.addNS(dnsDomain.dnsName, address, dnsName);
			}
		}

		Iterable<DnsRecord> dnsRecords = platformLayer.listItems(DnsRecord.class);
		for (DnsRecord record : dnsRecords) {
			switch (record.getState()) {
			case DELETE_REQUESTED:
			case DELETED:
				log.info("Skipping record (deleted/deleting): " + record);
				continue;
			default:
				break;
			}

			String zone = toZone(record);
			if (!zone.equals(dnsZone)) {
				continue;
			}

			dnsFile.addA(record.dnsName, record.address);
		}

		return dnsFile;
	}

	public ZoneFile buildDnsFile(DnsRecord dnsRecord) throws OpsException {
		String zone = toZone(dnsRecord);

		for (DnsZone dnsZone : platformLayer.listItems(DnsZone.class)) {
			if (dnsZone.dnsName.equals(zone)) {
				return buildDnsFile(dnsZone);
			}
		}

		throw new OpsException("Cannot find zone for record: " + dnsRecord.dnsName);
	}

	class TargetServer implements AutoCloseable {
		final OpsTarget target;

		public TargetServer(OpsTarget target) {
			this.target = target;
		}

		File tempDir;

		public File getTempDir() throws OpsException {
			if (tempDir == null) {
				tempDir = getTarget().createTempDir();
				OpsContext.get().takeOwnership(this);
			}
			return tempDir;
		}

		@Override
		public void close() throws OpsException {
			if (tempDir != null) {
				getTarget().rmdir(tempDir);
				tempDir = null;
			}
		}

		public OpsTarget getTarget() {
			return target;
		}

	}

	public void uploadToAllDnsServers(ZoneFile dnsFile) throws OpsException {
		// TODO: Likely problems with concurrent creation of servers and domain entries??

		boolean failed = false;

		for (DnsServer dnsServer : platformLayer.listItems(DnsServer.class)) {
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

			// TODO: We could have a 'cluster key' that is used to tag the
			// machines providing this service

			Machine machine = instances.findMachine(dnsServer);
			if (machine == null) {
				log.warn("Dns server instance not found: " + dnsServer);
				failed = true;
				continue;
			}

			OpsTarget target = machine.getTarget(service.getSshKey());

			upload(target, dnsFile);
		}

		if (failed) {
			throw new OpsException("Could not update all DNS servers in cluster").setRetry(TimeSpan.ONE_MINUTE);
		}
	}
}
