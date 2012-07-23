package org.platformlayer.ops.schedule;

import com.google.inject.ImplementedBy;

@ImplementedBy(SchedulerImpl.class)
public interface Scheduler {
	void putJob(String key, JobScheduleCalculator schedule, Runnable runnable);
}