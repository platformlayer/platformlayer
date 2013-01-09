package org.platformlayer.service.dns.ops;

import java.io.File;
import java.util.List;

import javalang7.AutoCloseable;

import javax.inject.Inject;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class DnsHelpers {

	private static final Logger log = LoggerFactory.getLogger(DnsHelpers.class);

	@Inject
	CloudContext cloud;

	@Inject
	ServiceContext service;

	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	InstanceHelpers instances;

	public void upload(OpsTarget target, ZoneFile zoneFile) throws OpsException {
		upload(new TargetServer(target), zoneFile);
	}

	public void upload(TargetServer targetServer, ZoneFile zoneFile) throws OpsException {
		// TODO: Validate / sanitize key
		File path = new File(DnsServerTemplate.getZonesDir(), zoneFile.getKey());
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

	public ZoneFile buildDnsFile(DnsZone dnsZone) throws OpsException {
		ZoneFile dnsFile = new ZoneFile(dnsZone.dnsName);

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
				dnsFile.addNS(dnsZone.dnsName, address, dnsName);
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

			if (!isInZone(record, dnsZone)) {
				continue;
			}

			dnsFile.addAddress(record.dnsName, record.address);
		}

		return dnsFile;
	}

	public ZoneFile buildDnsFile(DnsRecord dnsRecord) throws OpsException {
		List<DnsZone> matches = Lists.newArrayList();

		for (DnsZone dnsZone : platformLayer.listItems(DnsZone.class)) {
			if (isInZone(dnsRecord, dnsZone)) {
				matches.add(dnsZone);
			}
		}

		if (matches.size() == 0) {
			throw new OpsException("Cannot find zone for record: " + dnsRecord.dnsName);
		}

		if (matches.size() != 1) {
			throw new OpsException("Picking between multiple matching zones not yet implemented");
		}

		DnsZone dnsZone = matches.get(0);

		return buildDnsFile(dnsZone);
	}

	private boolean isInZone(DnsRecord dnsRecord, DnsZone dnsZone) {
		String dnsRecordName = dnsRecord.dnsName;
		String dnsZoneName = dnsZone.dnsName;
		if (dnsRecordName.equals(dnsZoneName)) {
			return true;
		} else if (dnsRecordName.endsWith("." + dnsZoneName)) {
			int firstDot = dnsRecordName.indexOf('.');
			if (firstDot != -1) {
				if (dnsZoneName.equals(dnsRecordName.substring(firstDot + 1))) {
					return true;
				}
			}
		}
		return false;
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
