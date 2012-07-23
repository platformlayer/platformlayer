package org.platformlayer.ops.schedule;

import org.platformlayer.core.model.Action;

public class ScheduledAction {
	final JobScheduleCalculator schedule;
	final Action action;

	public ScheduledAction(JobScheduleCalculator schedule, Action action) {
		super();
		this.schedule = schedule;
		this.action = action;
	}

	public JobScheduleCalculator getSchedule() {
		return schedule;
	}

}
