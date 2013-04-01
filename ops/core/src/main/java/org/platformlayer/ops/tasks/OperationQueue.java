package org.platformlayer.ops.tasks;

import java.util.List;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.jobs.model.JobData;
import org.platformlayer.jobs.model.JobExecutionData;
import org.platformlayer.jobs.model.JobExecutionList;
import org.platformlayer.jobs.model.JobLog;
import org.platformlayer.jobs.model.JobState;
import org.platformlayer.model.ProjectAuthorization;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.log.JobLogger;

import com.fathomdb.TimeSpan;

public interface OperationQueue {
	void submit(ProjectAuthorization auth, JobData jobData) throws OpsException;

	JobExecutionList listRecentExecutions(JobQuery query) throws OpsException;

	List<JobData> listRecentJobs(JobQuery query) throws OpsException;

	void jobFinished(JobExecutionData jobExecutionData, JobState state, JobLogger logger) throws OpsException;

	void submitRetry(ProjectAuthorization auth, JobData jobData, TimeSpan delay) throws OpsException;

	JobLog getActiveJobLog(PlatformLayerKey jobKey, String executionId);

	void startJob(JobQueueEntry entity) throws OpsException;

	JobQueueEntry take() throws OpsException;
}
