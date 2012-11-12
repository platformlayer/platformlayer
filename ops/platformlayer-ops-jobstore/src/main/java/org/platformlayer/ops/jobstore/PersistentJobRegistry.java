package org.platformlayer.ops.jobstore;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.platformlayer.RepositoryException;
import org.platformlayer.common.JobState;
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
import org.platformlayer.ops.log.JobLogger;
import org.platformlayer.ops.tasks.ActiveJobExecution;
import org.platformlayer.ops.tasks.JobRegistry;
import org.platformlayer.ops.tasks.OperationQueue;
import org.platformlayer.ops.tasks.OperationWorker;
import org.platformlayer.ops.tasks.OpsContextBuilder;
import org.platformlayer.xaas.repository.JobRepository;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Singleton
public class PersistentJobRegistry implements JobRegistry {
	@Inject
	OpsSystem opsSystem;

	@Inject
	JobRepository repository;

	@Inject
	JobLogStore jobLogStore;

	final Map<PlatformLayerKey, ActiveJobExecution> activeJobs = Maps.newHashMap();
	final LinkedList<JobExecutionData> recentJobs = Lists.newLinkedList();

	@Inject
	OpsContextBuilder opsContextBuilder;

	@Inject
	OperationQueue operationQueue;

	@Override
	public JobExecutionData enqueueOperation(Action action, ProjectAuthorization auth, PlatformLayerKey targetItem)
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

		Date startedAt = new Date();
		String executionId;
		try {
			executionId = repository.insertExecution(jobData.key, startedAt);
		} catch (RepositoryException e) {
			throw new OpsException("Error inserting job execution into repository", e);
		}

		JobExecutionData execution = new JobExecutionData();
		execution.job = jobData;
		execution.jobKey = jobData.getJobKey();
		execution.startedAt = startedAt;
		execution.executionId = executionId;
		execution.state = JobState.PRESTART;

		PersistentActiveJob activeJob = new PersistentActiveJob(this, auth, execution);
		activeJobs.put(execution.jobKey, activeJob);

		OperationWorker operationWorker = new OperationWorker(opsSystem, activeJob);
		operationQueue.submit(operationWorker);
		return execution;
	}

	private static final int RECENT_JOB_COUNT = 100;

	@Override
	public List<JobData> listJobs(ProjectId projectId) {
		List<JobData> ret = Lists.newArrayList();

		synchronized (activeJobs) {
			for (ActiveJobExecution job : activeJobs.values()) {
				if (!Objects.equal(job.getTargetItemKey().getProject(), projectId)) {
					continue;
				}
				JobExecutionData execution = job.getJobExecution();
				ret.add(execution.getJob());
			}
		}

		synchronized (recentJobs) {
			for (JobExecutionData job : recentJobs) {
				if (!job.getJobKey().getProject().equals(projectId)) {
					continue;
				}
				ret.add(job.getJob());
			}
		}

		return ret;
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
		ActiveJobExecution activeJobExecution = activeJobs.get(jobKey);
		if (activeJobExecution != null) {
			SimpleJobLogger logger = (SimpleJobLogger) activeJobExecution.getLogger();
			JobLog log = new JobLog();
			log.lines = Lists.newArrayList(Iterables.skip(logger.getLogEntries(), logSkip));
			log.execution = activeJobExecution.getJobExecution();
			return log;
		}

		JobExecutionData execution = findExecution(jobKey, executionId);
		Date startedAt = execution.getStartedAt();
		try {
			JobLog log = jobLogStore.getJobLog(startedAt, jobKey, executionId, logSkip);
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

	void jobFinished(PersistentActiveJob persistentActiveJob) throws OpsException {
		JobExecutionData jobData = persistentActiveJob.getJobExecution();
		PlatformLayerKey jobKey = jobData.getJobKey();
		String executionId = persistentActiveJob.getExecutionId();

		Date startTime = persistentActiveJob.getStartedAt();
		Date endTime = new Date();

		synchronized (activeJobs) {
			activeJobs.remove(jobKey);
		}

		synchronized (recentJobs) {
			recentJobs.push(jobData);
			if (recentJobs.size() > RECENT_JOB_COUNT) {
				recentJobs.pop();
			}
		}

		try {
			JobLogger logger = persistentActiveJob.getLogger();
			jobLogStore.saveJobLog(jobKey, executionId, startTime, logger);
			repository.recordJobEnd(jobKey, executionId, endTime, persistentActiveJob.getState());
		} catch (RepositoryException e) {
			throw new OpsException("Error writing job to repository", e);
		} catch (Exception e) {
			throw new OpsException("Error writing job log", e);
		}

	}

	@Override
	public JobExecutionList listExecutions(ProjectId projectId) {
		JobExecutionList ret = JobExecutionList.create();

		synchronized (activeJobs) {
			for (ActiveJobExecution job : activeJobs.values()) {
				if (!Objects.equal(job.getTargetItemKey().getProject(), projectId)) {
					continue;
				}
				ret.add(job.getJobExecution());
			}
		}

		synchronized (recentJobs) {
			for (JobExecutionData jobExecution : recentJobs) {
				if (!jobExecution.getJobKey().getProject().equals(projectId)) {
					continue;
				}
				ret.add(jobExecution);
			}
		}

		return ret;
	}
}
