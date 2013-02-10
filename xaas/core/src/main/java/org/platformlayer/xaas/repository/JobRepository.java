package org.platformlayer.xaas.repository;

import java.util.Date;
import java.util.List;

import org.platformlayer.RepositoryException;
import org.platformlayer.TimeSpan;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.jobs.model.JobData;
import org.platformlayer.jobs.model.JobExecutionData;
import org.platformlayer.jobs.model.JobState;

public interface JobRepository {
	// void recordJob(PlatformLayerKey jobId, PlatformLayerKey itemKey, JobState jobState, JobLog jobLog)
	// throws RepositoryException;

	List<JobExecutionData> listExecutions(PlatformLayerKey jobKey) throws RepositoryException;

	JobExecutionData findExecution(PlatformLayerKey jobKey, String runId) throws RepositoryException;

	JobData findJob(PlatformLayerKey jobKey) throws RepositoryException;

	void recordJobEnd(PlatformLayerKey jobKey, String executionId, Date endTime, JobState state)
			throws RepositoryException;

	String insertExecution(PlatformLayerKey jobKey, Date startedAt) throws RepositoryException;

	String insertJob(ProjectId projectId, JobData jobData) throws RepositoryException;

	List<JobExecutionData> listRecentExecutions(ProjectId projectId, TimeSpan window) throws RepositoryException;

	List<JobData> listRecentJobs(ProjectId projectId, TimeSpan window) throws RepositoryException;
}
