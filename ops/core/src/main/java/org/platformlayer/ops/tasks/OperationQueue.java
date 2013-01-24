package org.platformlayer.ops.tasks;

import java.util.List;

import org.platformlayer.TimeSpan;
import org.platformlayer.common.JobState;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.jobs.model.JobData;
import org.platformlayer.jobs.model.JobExecutionData;
import org.platformlayer.jobs.model.JobExecutionList;
import org.platformlayer.jobs.model.JobLog;
import org.platformlayer.model.ProjectAuthorization;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.log.JobLogger;

public interface OperationQueue {
	void submit(ProjectAuthorization auth, JobData jobData) throws OpsException;

	JobExecutionList listRecentExecutions(ProjectId projectId) throws OpsException;

	List<JobData> listRecentJobs(ProjectId projectId) throws OpsException;

	void jobFinished(JobExecutionData jobExecutionData, JobState state, JobLogger logger) throws OpsException;

	void submitRetry(ProjectAuthorization auth, JobData jobData, TimeSpan delay) throws OpsException;

	JobLog getActiveJobLog(PlatformLayerKey jobKey, String executionId);

	void startJob(JobQueueEntry entity) throws OpsException;

	JobQueueEntry take() throws OpsException;
}
