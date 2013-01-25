package org.platformlayer.ops.jobstore;

import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import org.platformlayer.PrimitiveComparators;
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
import org.platformlayer.ops.tasks.JobQueueEntry;
import org.platformlayer.ops.tasks.OperationWorker;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class SimpleOperationQueue extends OperationQueueBase {
	@Inject
	ExecutorService executorService;

	@Inject
	OpsSystem opsSystem;

	@Inject
	JobLogStore jobLogStore;

	final PriorityQueue<QueuedJob> queue = new PriorityQueue<QueuedJob>(100, new Comparator<QueuedJob>() {
		@Override
		public int compare(QueuedJob o1, QueuedJob o2) {
			long t1 = o1.timestamp;
			long t2 = o2.timestamp;

			return PrimitiveComparators.compare(t1, t2);
		}
	});

	final Map<PlatformLayerKey, ActiveJobExecution> activeJobs = Maps.newHashMap();
	final LinkedList<JobExecutionData> recentJobs = Lists.newLinkedList();

	class QueuedJob implements Callable<Object>, JobQueueEntry {
		final ProjectAuthorization auth;
		final JobData jobData;
		final long timestamp;

		QueuedJob(long timestamp, ProjectAuthorization auth, JobData jobData) {
			super();
			this.timestamp = timestamp;
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
		long timestamp = System.currentTimeMillis();

		QueuedJob queuedJob = new QueuedJob(timestamp, auth, jobData);
		synchronized (queue) {
			queue.add(queuedJob);
		}
	}

	@Override
	public void submitRetry(ProjectAuthorization auth, JobData jobData, TimeSpan delay) {
		long timestamp = System.currentTimeMillis();
		timestamp += delay.getTotalMilliseconds();

		final QueuedJob queuedJob = new QueuedJob(timestamp, auth, jobData);
		synchronized (queue) {
			queue.add(queuedJob);
		}
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

	@Override
	public void startJob(JobQueueEntry entity) throws OpsException {
		QueuedJob job = (QueuedJob) entity;
		executorService.submit(job);
	}

	@Override
	public JobQueueEntry take() throws OpsException {
		synchronized (queue) {
			QueuedJob job = queue.peek();
			if (job == null) {
				return null;
			}

			long now = System.currentTimeMillis();
			if (now < job.timestamp) {
				return null;
			}

			return queue.remove();
		}
	}

}