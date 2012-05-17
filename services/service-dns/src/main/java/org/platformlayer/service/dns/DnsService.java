package org.platformlayer.service.dns;

import org.platformlayer.Strings;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.ServiceProviderBase;
import org.platformlayer.service.dns.model.DnsRecord;
import org.platformlayer.service.dns.model.DnsServer;
import org.platformlayer.service.dns.model.DnsZone;
import org.platformlayer.xaas.Service;

@Service("dns")
public class DnsService extends ServiceProviderBase {

	@Override
	public void beforeCreateItem(ItemBase item) throws OpsException {
		if (item instanceof DnsServer) {
			DnsServer model = (DnsServer) item;

			model.dnsName = normalize(model.dnsName);
			if (Strings.isEmpty(model.dnsName)) {
				throw new IllegalArgumentException("dnsName must be specified");
			}
		}

		if (item instanceof DnsRecord) {
			DnsRecord model = (DnsRecord) item;

			model.dnsName = normalize(model.dnsName);
			if (Strings.isEmpty(model.dnsName)) {
				throw new IllegalArgumentException("dnsName must be specified");
			}

			if (Strings.isEmpty(model.recordType)) {
				model.recordType = "A";
			}

			if (model.recordType.equals("A")) {
				// OK
			} else {
				throw new IllegalArgumentException("Record type not supported: " + model.recordType);
			}
		}

		if (item instanceof DnsZone) {
			DnsZone model = (DnsZone) item;

			model.dnsName = normalize(model.dnsName);
			if (Strings.isEmpty(model.dnsName)) {
				model.dnsName = normalize(model.getId());
			}
		}

		super.beforeCreateItem(item);
	}

	private String normalize(String dnsName) {
		if (dnsName == null) {
			return null;
		}

		dnsName = dnsName.trim();

		dnsName = dnsName.toLowerCase();
		if (dnsName.endsWith(".")) {
			dnsName = dnsName.substring(0, dnsName.length() - 1);
		}

		return dnsName;
	}

}
