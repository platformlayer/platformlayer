package org.platformlayer.service.instancesupervisor.ops;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistentInstanceController extends OpsTreeBase {

	private static final Logger log = LoggerFactory.getLogger(PersistentInstanceController.class);

	@Handler
	public void handler() {

	}

	@Override
	protected void addChildren() throws OpsException {
		{
			PersistentInstanceMapper instance = injected(PersistentInstanceMapper.class);
			addChild(instance);
		}

		// This isn't the right place for DNS, now we have endpoints

		// PersistentInstance model = OpsContext.get().getInstance(PersistentInstance.class);

		// if (!Strings.isNullOrEmpty(model.dnsName)) {
		// // Create a DNS record
		// Tag childTag = opsContext.getOpsSystem().createParentTag(model);
		//
		// List<DnsRecord> dnsRecords = platformLayer.listItems(DnsRecord.class, childTag);
		//
		// // TODO: Wrong address?
		//
		// if (dnsRecords.isEmpty()) {
		// String address = machine.getAddress(NetworkPoint.forPublicInternet(), 0);
		//
		// DnsRecord record = new DnsRecord();
		// record.setDnsName(model.dnsName);
		// record.getAddress().add(address);
		// record.getTags().add(childTag);
		//
		// try {
		// platformLayer.create(record);
		// } catch (OpenstackClientException e) {
		// throw new OpsException("Error registering persistent instance", e);
		// }
		// }
		// }
	}

}
