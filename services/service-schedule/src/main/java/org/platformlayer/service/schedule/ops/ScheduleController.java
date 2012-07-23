package org.platformlayer.service.schedule.ops;

import java.util.Date;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.PlatformLayerEndpointInfo;
import org.platformlayer.TimeSpan;
import org.platformlayer.core.model.Action;
import org.platformlayer.core.model.JobSchedule;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.Bound;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.schedule.ActionScheduler;
import org.platformlayer.ops.schedule.JobScheduleCalculator;
import org.platformlayer.ops.schedule.SimpleJobScheduleCalculator;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.schedule.model.ScheduledTask;

import com.google.common.base.Strings;

public class ScheduleController extends OpsTreeBase {
	static final Logger log = Logger.getLogger(ScheduleController.class);

	@Inject
	ActionScheduler actionScheduler;

	@Bound
	ScheduledTask model;

	@Inject
	PlatformLayerHelpers platformLayer;

	@Handler
	public void handler() {
		if (OpsContext.isConfigure()) {
			String key = model.getKey().getUrl();
			PlatformLayerKey target = model.targetItem;
			PlatformLayerEndpointInfo endpoint = platformLayer.getEndpointInfo(target);
			JobScheduleCalculator scheduleCalculator = parseSchedule();
			Action action = model.action;

			actionScheduler.putJob(key, endpoint, scheduleCalculator, target, action);
		}
	}

	private JobScheduleCalculator parseSchedule() {
		JobSchedule schedule = model.schedule;

		// Default to every hour
		TimeSpan interval = TimeSpan.ONE_HOUR;
		Date base = null;

		if (schedule != null) {
			if (!Strings.isNullOrEmpty(schedule.interval)) {
				interval = TimeSpan.parse(schedule.interval);
			}

			if (schedule.base != null) {
				base = schedule.base;
			}
		}

		JobScheduleCalculator scheduleCalculator = new SimpleJobScheduleCalculator(interval, base);
		return scheduleCalculator;
	}

	@Override
	protected void addChildren() throws OpsException {
	}
}
