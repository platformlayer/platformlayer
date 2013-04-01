package org.platformlayer.ops.jobstore;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.platformlayer.RepositoryException;
import org.platformlayer.core.model.Action;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ids.ManagedItemId;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.jobs.model.JobData;
import org.platformlayer.jobs.model.JobExecutionData;
import org.platformlayer.jobs.model.JobExecutionList;
import org.platformlayer.jobs.model.JobLog;
import org.platformlayer.model.ProjectAuthorization;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.tasks.ActiveJobExecution;
import org.platformlayer.ops.tasks.JobQuery;
import org.platformlayer.ops.tasks.JobRegistry;
import org.platformlayer.ops.tasks.OperationQueue;
import org.platformlayer.ops.tasks.OpsContextBuilder;
import org.platformlayer.xaas.repository.JobRepository;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Singleton
public class PersistentJobRegistry implements JobRegistry {
	@Inject
	OpsSystem opsSystem;

	@Inject
	JobRepository repository;

	@Inject
	JobLogStore jobLogStore;

	@Inject
	OpsContextBuilder opsContextBuilder;

	@Inject
	OperationQueue operationQueue;

	@Override
	public JobData enqueueOperation(Action action, ProjectAuthorization auth, PlatformLayerKey targetItem)
			throws OpsException {
		ProjectId projectId;
		try {
			projectId = opsContextBuilder.getRunAsProjectId(auth);
		} catch (OpsException e) {
			throw new OpsException("Error getting projectId", e);
		}

		JobData jobData = new JobData();
		jobData.action = action;
		jobData.targetId = targetItem;

		try {
			String jobId = repository.insertJob(projectId, jobData);
			jobData.key = JobData.buildKey(projectId, new ManagedItemId(jobId));
		} catch (RepositoryException e) {
			throw new OpsException("Error inserting job", e);
		}

		operationQueue.submit(auth, jobData);

		return jobData;
	}

	@Override
	public List<JobData> listRecentJobs(JobQuery jobQuery) throws OpsException {
		return operationQueue.listRecentJobs(jobQuery);
	}

	// @Override
	// JobRecord startJob(JobRecord jobRecord) {
	// PlatformLayerKey jobKey = jobRecord.getJobKey();
	//
	// if (jobKey != null) {
	// synchronized (activeJobs) {
	// activeJobs.put(jobKey, jobRecord);
	// }
	// }
	//
	// return jobRecord;
	// }

	// public JobData getJob(PlatformLayerKey jobKey, boolean fetchLog) {
	// JobData jobData = null;
	// synchronized (activeJobs) {
	// jobRecord = activeJobs.get(jobKey);
	// }
	//
	// if (jobData == null) {
	// synchronized (recentJobs) {
	// for (JobData recentJob : recentJobs) {
	// if (recentJob.getJobKey().equals(jobKey)) {
	// jobData = recentJob;
	// break;
	// }
	// }
	// }
	// }
	//
	// if (jobData == null) {
	// throw new UnsupportedOperationException();
	//
	// // jobRecord = repository.getJob(jobId, fetchLog);
	// }
	//
	// return jobData;
	// }

	@Override
	public JobLog getJobLog(PlatformLayerKey jobKey, String executionId, int logSkip) throws OpsException {
		JobExecutionData execution = findExecution(jobKey, executionId);

		Date startedAt = execution.getStartedAt();
		if (execution.getEndedAt() == null) {
			JobLog log = operationQueue.getActiveJobLog(jobKey, executionId);
			if (log != null) {
				JobLog ret = new JobLog();
				ret.lines = Lists.newArrayList(Iterables.skip(log.lines, logSkip));
				ret.execution = log.execution;
				return ret;
			}
		}

		try {
			String cookie = execution.logCookie;
			JobLog log = jobLogStore.getJobLog(startedAt, jobKey, executionId, cookie, logSkip);
			if (log != null) {
				log.execution = execution;
			}
			return log;
		} catch (IOException e) {
			throw new OpsException("Error reading job log", e);
		}
	}

	@Override
	public List<JobExecutionData> listExecutions(PlatformLayerKey jobKey) throws OpsException {
		try {
			return repository.listExecutions(jobKey);
		} catch (RepositoryException e) {
			throw new OpsException("Error retrieving job executions", e);
		}
	}

	@Override
	public JobExecutionData findExecution(PlatformLayerKey jobKey, String runId) throws OpsException {
		try {
			return repository.findExecution(jobKey, runId);
		} catch (RepositoryException e) {
			throw new OpsException("Error retrieving job execution", e);
		}
	}

	@Override
	public JobData getJob(PlatformLayerKey jobKey) throws OpsException {
		try {
			return repository.findJob(jobKey);
		} catch (RepositoryException e) {
			throw new OpsException("Error retrieving job", e);
		}
	}

	@Override
	public ActiveJobExecution startSystemJob(ServiceType serviceType, ProjectAuthorization authentication) {
		String executionId = null;
		return new PersistentActiveJob(serviceType, authentication, executionId);
	}

	@Override
	public JobExecutionList listRecentExecutions(JobQuery jobQuery) throws OpsException {
		return operationQueue.listRecentExecutions(jobQuery);
	}
}
