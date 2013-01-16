package org.platformlayer.service.dns;

import java.util.List;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.dns.model.DnsRecord;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.ServiceProviderBase;
import org.platformlayer.service.dns.model.DnsServer;
import org.platformlayer.service.dns.model.DnsZone;
import org.platformlayer.service.dns.ops.DnsRecordController;
import org.platformlayer.xaas.Service;
import org.platformlayer.xaas.services.ModelClass;

import com.google.common.base.Strings;

@Service("dns")
public class DnsService extends ServiceProviderBase {
	@Override
	protected List<ModelClass<?>> buildModels() {
		List<ModelClass<?>> modelClasses = super.buildModels();

		modelClasses.add(ModelClass.publicModel(this, DnsRecord.class));

		return modelClasses;
	}

	@Override
	public Class<?> getControllerClass(Class<?> managedItemClass) throws OpsException {
		ensureInitialized();

		if (managedItemClass == DnsRecord.class) {
			return DnsRecordController.class;
		}

		return super.getControllerClass(managedItemClass);
	}

	@Override
	public void beforeCreateItem(ItemBase item) throws OpsException {
		if (item instanceof DnsServer) {
			DnsServer model = (DnsServer) item;

			model.dnsName = normalize(model.dnsName);
			if (Strings.isNullOrEmpty(model.dnsName)) {
				throw new IllegalArgumentException("dnsName must be specified");
			}
		}

		if (item instanceof DnsRecord) {
			DnsRecord model = (DnsRecord) item;

			model.dnsName = normalize(model.dnsName);
			if (Strings.isNullOrEmpty(model.dnsName)) {
				throw new IllegalArgumentException("dnsName must be specified");
			}

			if (Strings.isNullOrEmpty(model.recordType)) {
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
			if (Strings.isNullOrEmpty(model.dnsName)) {
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
