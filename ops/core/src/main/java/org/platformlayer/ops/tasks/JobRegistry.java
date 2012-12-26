package org.platformlayer.ops.tasks;

import java.util.List;

import org.platformlayer.TimeSpan;
import org.platformlayer.core.model.Action;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.jobs.model.JobData;
import org.platformlayer.jobs.model.JobExecutionData;
import org.platformlayer.jobs.model.JobExecutionList;
import org.platformlayer.jobs.model.JobLog;
import org.platformlayer.model.ProjectAuthorization;
import org.platformlayer.ops.OpsException;

public interface JobRegistry {
	List<JobExecutionData> listExecutions(PlatformLayerKey jobKey) throws OpsException;

	JobExecutionData findExecution(PlatformLayerKey jobKey, String runId) throws OpsException;

	JobLog getJobLog(PlatformLayerKey jobKey, String executionId, int logSkip) throws OpsException;

	JobData getJob(PlatformLayerKey jobKey) throws OpsException;

	List<JobData> listJobs(ProjectId project);

	JobExecutionData enqueueOperation(Action action, ProjectAuthorization projectAuthorization, PlatformLayerKey itemKey)
			throws OpsException;

	void enqueueRetry(ActiveJobExecution activeJob, TimeSpan delay) throws OpsException;

	ActiveJobExecution startSystemJob(ServiceType serviceType, ProjectAuthorization authentication);

	JobExecutionList listExecutions(ProjectId project);
}
