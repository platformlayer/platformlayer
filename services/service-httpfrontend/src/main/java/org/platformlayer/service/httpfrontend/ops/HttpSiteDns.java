package org.platformlayer.service.httpfrontend.ops;

import javax.inject.Inject;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.UniqueTag;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.tree.OwnedItem;
import org.platformlayer.service.dns.v1.DnsRecord;
import org.platformlayer.service.httpfrontend.model.HttpServer;
import org.platformlayer.service.httpfrontend.model.HttpSite;

public class HttpSiteDns extends OwnedItem<DnsRecord> {

	@Inject
	InstanceHelpers instanceHelpers;

	@Override
	protected DnsRecord buildItemTemplate() throws OpsException {
		// TODO: Idempotency etc
		HttpServer server = OpsContext.get().getInstance(HttpServer.class);
		HttpSite site = OpsContext.get().getInstance(HttpSite.class);

		Machine machine = instanceHelpers.getMachine(server);

		String address = machine.getAddress(NetworkPoint.forPublicInternet(), 0);

		DnsRecord record = new DnsRecord();
		record.setDnsName(site.hostname);
		record.getAddress().add(address);

		Tag parentTag = Tag.buildParentTag(OpsSystem.toKey(site));
		record.getTags().add(parentTag);
		Tag uniqueTag = UniqueTag.build(server, site);
		record.getTags().add(uniqueTag);

		record.key = PlatformLayerKey.fromId(site.hostname);

		return record;
	}

}
