package org.platformlayer.ops.schedule;

import org.platformlayer.ops.OpsException;

import com.google.inject.ImplementedBy;

@ImplementedBy(SchedulerImpl.class)
public interface Scheduler {
	void putJob(SchedulerRecord record) throws OpsException;

	void start() throws OpsException;
}