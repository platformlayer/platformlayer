package org.platformlayer.ops.tasks;

import org.platformlayer.core.model.Action;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.jobs.model.JobExecutionData;
import org.platformlayer.jobs.model.JobState;
import org.platformlayer.model.ProjectAuthorization;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.log.JobLogger;

import com.fathomdb.TimeSpan;

public interface ActiveJobExecution {
	Action getAction();

	PlatformLayerKey getTargetItemKey();

	ProjectAuthorization getProjectAuthorization();

	ServiceType getServiceType();

	void setState(JobState success);

	JobLogger getLogger();

	JobState getState();

	String getExecutionId();

	void recordJobEnd() throws OpsException;

	JobExecutionData getJobExecution();

	void enqueueRetry(TimeSpan delay) throws OpsException;
}
