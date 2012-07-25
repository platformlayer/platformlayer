package org.platformlayer.service.schedule.ops;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.PlatformLayerEndpointInfo;
import org.platformlayer.core.model.Action;
import org.platformlayer.core.model.JobSchedule;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.Bound;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.schedule.ActionScheduler;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.schedule.model.ScheduledTask;

public class ScheduleController extends OpsTreeBase {
	static final Logger log = Logger.getLogger(ScheduleController.class);

	@Inject
	ActionScheduler actionScheduler;

	@Bound
	ScheduledTask model;

	@Inject
	PlatformLayerHelpers platformLayer;

	@Handler
	public void handler() throws OpsException {
		if (OpsContext.isConfigure()) {
			String key = model.getKey().getUrl();
			PlatformLayerKey target = model.targetItem;
			PlatformLayerEndpointInfo endpoint = platformLayer.getEndpointInfo(target);
			JobSchedule schedule = model.schedule;
			Action action = model.action;

			actionScheduler.putJob(key, endpoint, schedule, target, action);
		}
	}

	@Override
	protected void addChildren() throws OpsException {
	}
}
