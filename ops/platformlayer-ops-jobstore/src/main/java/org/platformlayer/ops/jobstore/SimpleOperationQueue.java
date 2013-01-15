package org.platformlayer.ops.jobstore;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import org.platformlayer.RepositoryException;
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
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.log.JobLogger;
import org.platformlayer.ops.tasks.ActiveJobExecution;
import org.platformlayer.ops.tasks.OperationWorker;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class SimpleOperationQueue extends OperationQueueBase {
	@Inject
	ExecutorService executorService;

	@Inject
	OpsSystem opsSystem;

	// @Inject
	// JobRegistry jobRegistry;

	@Inject
	JobLogStore jobLogStore;

	final Timer timer = new Timer();

	final Map<PlatformLayerKey, ActiveJobExecution> activeJobs = Maps.newHashMap();
	final LinkedList<JobExecutionData> recentJobs = Lists.newLinkedList();

	class QueuedJob implements Callable<Object> {
		final ProjectAuthorization auth;
		final JobData jobData;

		QueuedJob(ProjectAuthorization auth, JobData jobData) {
			super();
			this.auth = auth;
			this.jobData = jobData;
		}

		@Override
		public Object call() throws Exception {
			JobExecutionData execution = createExecution(jobData);

			PersistentActiveJob activeJob = new PersistentActiveJob(SimpleOperationQueue.this, auth, execution);
			activeJobs.put(execution.jobKey, activeJob);

			OperationWorker operationWorker = new OperationWorker(opsSystem, activeJob);

			return operationWorker.call();
		}
	}

	@Override
	public void submit(ProjectAuthorization auth, JobData jobData) {
		QueuedJob queuedJob = new QueuedJob(auth, jobData);
		executorService.submit(queuedJob);
	}

	@Override
	public void submitRetry(ProjectAuthorization auth, JobData jobData, TimeSpan delay) {
		final QueuedJob queuedJob = new QueuedJob(auth, jobData);

		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				executorService.submit(queuedJob);
			}
		}, delay.getTotalMilliseconds());
	}

	@Override
	public JobExecutionList listRecentExecutions(ProjectId projectId) {
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

	@Override
	public List<JobData> listRecentJobs(ProjectId projectId) {
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

	private static final int RECENT_JOB_COUNT = 100;

	@Override
	public void jobFinished(JobExecutionData jobExecutionData, JobState state, JobLogger logger) throws OpsException {
		PlatformLayerKey jobKey = jobExecutionData.getJobKey();
		String executionId = jobExecutionData.getExecutionId();

		Date startTime = jobExecutionData.getStartedAt();
		Date endTime = new Date();

		synchronized (activeJobs) {
			activeJobs.remove(jobKey);
		}

		synchronized (recentJobs) {
			recentJobs.push(jobExecutionData);
			if (recentJobs.size() > RECENT_JOB_COUNT) {
				recentJobs.pop();
			}
		}

		try {
			jobLogStore.saveJobLog(jobKey, executionId, startTime, logger);
			jobRepository.recordJobEnd(jobKey, executionId, endTime, state);
		} catch (RepositoryException e) {
			throw new OpsException("Error writing job to repository", e);
		} catch (Exception e) {
			throw new OpsException("Error writing job log", e);
		}
	}

	@Override
	public JobLog getActiveJobLog(PlatformLayerKey jobKey, String executionId) {
		ActiveJobExecution activeJobExecution = activeJobs.get(jobKey);
		if (activeJobExecution != null) {
			SimpleJobLogger logger = (SimpleJobLogger) activeJobExecution.getLogger();
			JobLog log = new JobLog();
			log.lines = Lists.newArrayList(logger.getLogEntries());
			log.execution = activeJobExecution.getJobExecution();
			return log;
		}

		return null;
	}

}
