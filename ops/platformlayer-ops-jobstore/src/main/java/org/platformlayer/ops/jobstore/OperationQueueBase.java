package org.platformlayer.ops.jobstore;

import java.util.Date;

import javax.inject.Inject;

import org.platformlayer.RepositoryException;
import org.platformlayer.common.JobState;
import org.platformlayer.jobs.model.JobData;
import org.platformlayer.jobs.model.JobExecutionData;
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

}
