package org.platformlayer.ops.machines;

import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.core.model.MachineCloudBase;
import org.platformlayer.ops.MachineCreationRequest;
import org.platformlayer.ops.OpsException;
import org.platformlayer.service.machines.direct.v1.DirectCloud;
import org.platformlayer.service.machines.openstack.v1.OpenstackCloud;
import org.platformlayer.service.machines.raw.v1.RawCloud;

import com.google.common.collect.Lists;

public class SimpleMultiCloudScheduler implements MultiCloudScheduler {
	static final Logger log = Logger.getLogger(SimpleMultiCloudScheduler.class);

	@Inject
	PlatformLayerHelpers platformLayerHelpers;

	@Inject
	PlatformLayerClient platformLayerClient;

	@Inject
	PlatformLayerCloudHelpers cloudHelpers;

	@Override
	public List<MachineCloudBase> listClouds() throws OpsException {
		return cloudHelpers.findClouds(null);
	}

	@Override
	public MachineCloudBase pickCloud(MachineCreationRequest request) throws OpsException {
		if (request.cloud == null) {
			List<MachineCloudBase> clouds = listClouds();

			if (clouds.size() == 0) {
				throw new OpsException("No cloud configured");
			} else if (clouds.size() == 1) {
				return clouds.get(0);
			} else {
				// TODO: This is a total hack
				// TODO: How to implement this? Look at "price" ?

				log.warn("Cloud selection is primitive");

				List<MachineCloudBase> best = Lists.newArrayList();
				float bestCost = Float.MAX_VALUE;
				for (MachineCloudBase candidate : clouds) {
					float cost = getPrice(candidate);

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

		MachineCloudBase cloud = platformLayerHelpers.getItem(request.cloud, MachineCloudBase.class);
		return cloud;
	}

	private float getPrice(MachineCloudBase candidate) {
		float cost;
		if (candidate.getClass().getSimpleName().equals(RawCloud.class.getSimpleName())) {
			cost = 100;
		} else if (candidate.getClass().getSimpleName().equals(DirectCloud.class.getSimpleName())) {
			cost = 10;
		} else if (candidate.getClass().getSimpleName().equals(OpenstackCloud.class.getSimpleName())) {
			cost = 9;
		} else {
			throw new IllegalStateException();
		}
		return cost;

	}

}
