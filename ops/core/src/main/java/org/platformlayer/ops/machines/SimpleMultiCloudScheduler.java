package org.platformlayer.ops.machines;

import java.util.List;

import javax.inject.Inject;

import org.platformlayer.PlatformLayerClient;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.ops.MachineCreationRequest;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.helpers.ProviderHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class SimpleMultiCloudScheduler implements MultiCloudScheduler {
	static final Logger log = LoggerFactory.getLogger(SimpleMultiCloudScheduler.class);

	@Inject
	PlatformLayerHelpers platformLayerHelpers;

	@Inject
	PlatformLayerClient platformLayerClient;

	@Inject
	PlatformLayerCloudHelpers cloudHelpers;

	@Inject
	ProviderHelper providers;

	@Override
	public MachineProvider pickCloud(MachineCreationRequest request) throws OpsException {
		if (request.cloud == null) {
			List<MachineProvider> clouds = cloudHelpers.findClouds();

			if (clouds.size() == 0) {
				throw new OpsException("No cloud configured");
			} else if (clouds.size() == 1) {
				return clouds.get(0);
			} else {
				// TODO: This is a total hack
				// TODO: How to implement this? Look at "price" ?

				log.warn("Cloud selection is primitive");

				List<MachineProvider> best = Lists.newArrayList();
				float bestCost = Float.MAX_VALUE;
				for (MachineProvider candidate : clouds) {
					float cost = candidate.getPrice(request);

					if (cost < bestCost) {
						bestCost = cost;
						best = Lists.newArrayList();
					}

					if (cost <= bestCost) {
						best.add(candidate);
					}
				}

				if (best.size() == 0) {
					throw new IllegalStateException();
				}

				if (best.size() != 1) {
					throw new OpsException("Cannot choose cloud");
				}

				return best.get(0);
			}
		}

		ItemBase item = platformLayerHelpers.getItem(request.cloud);
		return providers.toInterface(item, MachineProvider.class);
	}

}
