package org.platformlayer.ops.jobstore;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.platformlayer.RepositoryException;
import org.platformlayer.TimeSpan;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.jobs.model.JobData;
import org.platformlayer.jobs.model.JobExecutionData;
import org.platformlayer.jobs.model.JobExecutionList;
import org.platformlayer.jobs.model.JobState;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.tasks.OperationQueue;
import org.platformlayer.xaas.repository.JobRepository;

public abstract class OperationQueueBase implements OperationQueue {
	@Inject
	protected JobRepository jobRepository;

	protected JobExecutionData createExecution(JobData jobData) throws OpsException {
		Date startedAt = new Date();
		String executionId;
		try {
			executionId = jobRepository.insertExecution(jobData.key, startedAt);
		} catch (RepositoryException e) {
			throw new OpsException("Error inserting job execution into repository", e);
		}

		JobExecutionData execution = new JobExecutionData();
		execution.job = jobData;
		execution.jobKey = jobData.getJobKey();
		execution.startedAt = startedAt;
		execution.executionId = executionId;
		execution.state = JobState.PRESTART;

		return execution;
	}

	@Override
	public JobExecutionList listRecentExecutions(ProjectId projectId) throws OpsException {
		JobExecutionList ret = JobExecutionList.create();

		List<JobExecutionData> jobs;
		try {
			jobs = jobRepository.listRecentExecutions(projectId, TimeSpan.FIVE_MINUTES);
		} catch (RepositoryException e) {
			throw new OpsException("Error querying for jobs", e);
		}

		for (JobExecutionData jobExecution : jobs) {
			// if (!jobExecution.getJobKey().getProject().equals(projectId)) {
			// throw new IllegalStateException();
			// }
			ret.add(jobExecution);
		}

		return ret;
	}

	@Override
	public List<JobData> listRecentJobs(ProjectId projectId) throws OpsException {
		List<JobData> jobs;
		try {
			jobs = jobRepository.listRecentJobs(projectId, TimeSpan.FIVE_MINUTES);
		} catch (RepositoryException e) {
			throw new OpsException("Error querying for jobs", e);
		}

		return jobs;
	}

}
