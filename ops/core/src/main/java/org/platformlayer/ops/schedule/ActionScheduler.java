package org.platformlayer.ops.schedule;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.HttpPlatformLayerClient;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.PlatformLayerEndpointInfo;
import org.platformlayer.core.model.Action;
import org.platformlayer.core.model.PlatformLayerKey;

public class ActionScheduler {
	private static final Logger log = Logger.getLogger(ActionScheduler.class);

	@Inject
	Scheduler scheduler;

	public void putJob(String key, final PlatformLayerEndpointInfo endpoint, JobScheduleCalculator schedule,
			final PlatformLayerKey target, final Action action) {
		// We could create a "per-job" "single-purpose" key??

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					PlatformLayerClient platformLayer = HttpPlatformLayerClient.build(
							endpoint.getPlatformlayerBaseUrl(), endpoint.getAuthenticator(), endpoint.getProjectId(),
							endpoint.getTrustKeys());

					platformLayer.doAction(target, action);
				} catch (PlatformLayerClientException e) {
					log.warn("Error running action", e);
				}
			}

			@Override
			public String toString() {
				return action + " on " + target;
			}
		};

		scheduler.putJob(key, schedule, runnable);
	}
}
