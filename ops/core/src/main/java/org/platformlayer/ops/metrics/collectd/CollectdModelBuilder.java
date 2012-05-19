package org.platformlayer.ops.metrics.collectd;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.templates.TemplateDataSource;
import org.platformlayer.service.collectd.v1.CollectdService;

public class CollectdModelBuilder implements TemplateDataSource {
	static final Logger log = Logger.getLogger(CollectdModelBuilder.class);

	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	InstanceHelpers instances;

	@Inject
	OpsSystem opsSystem;

	@Override
	public void buildTemplateModel(Map<String, Object> model) throws OpsException {
		model.put("collectdServer", getCollectdServer());
		model.put("collectdHostname", getCollectdHostKey());
	}

	private String getCollectdHostKey() {
		// TODO: Multiple machines per service
		ItemBase managed = OpsContext.get().getInstance(ItemBase.class);
		PlatformLayerKey modelKey = OpsSystem.toKey(managed);
		return CollectdHelpers.toCollectdKey(modelKey);
	}

	@Deprecated
	public String getCollectdServer() throws OpsException {
		Iterable<CollectdService> collectdServices = platformLayer.listItems(CollectdService.class);
		for (CollectdService collectdService : collectdServices) {
			// TODO: Use DNS name when it works
			Machine machine = instances.findMachine(collectdService);
			if (machine != null) {
				NetworkPoint targetNetworkPoint = NetworkPoint.forTargetInContext();
				List<InetAddress> addresses = machine.findAddresses(targetNetworkPoint, CollectdCommon.COLLECTD_PORT);
				if (!addresses.isEmpty()) {
					return addresses.get(0).getHostAddress();
				}
			}
		}

		log.warn("Unable to find collectd server; defaulting to 127.0.0.1");
		return "127.0.0.1";
	}
}
