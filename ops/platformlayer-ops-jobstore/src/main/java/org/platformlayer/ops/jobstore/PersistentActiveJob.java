package org.platformlayer.ops.jobstore;

import java.util.Date;

import org.platformlayer.common.JobState;
import org.platformlayer.core.model.Action;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.jobs.model.JobData;
import org.platformlayer.jobs.model.JobExecutionData;
import org.platformlayer.model.ProjectAuthorization;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.log.JobLogger;
import org.platformlayer.ops.tasks.ActiveJobExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistentActiveJob implements ActiveJobExecution {
	private static final Logger log = LoggerFactory.getLogger(PersistentActiveJob.class);

	final String executionId;
	final JobExecutionData jobExecution;
	final JobData jobData;

	final JobLogger logger;

	// This is awkward; we want a state even though we don't always have a jobExecutionData
	JobState state;

	private final ProjectAuthorization authentication;

	private final ServiceType serviceType;

	private final PersistentJobRegistry registry;

	private final Date startedAt;

	public PersistentActiveJob(PersistentJobRegistry registry, ProjectAuthorization authentication,
			JobExecutionData jobExecution) {
		super();
		this.registry = registry;
		this.authentication = authentication;
		this.jobData = jobExecution.getJob();
		this.jobExecution = jobExecution;
		this.executionId = jobExecution.getExecutionId();
		this.logger = new SimpleJobLogger();
		this.serviceType = jobData.getTargetItemKey().getServiceType();
		this.startedAt = jobExecution.getStartedAt();

		this.state = jobExecution.state;
	}

	public PersistentActiveJob(ServiceType serviceType, ProjectAuthorization authentication, String executionId) {
		super();
		this.registry = null;
		this.serviceType = serviceType;
		this.authentication = authentication;
		this.jobData = null;
		this.jobExecution = null;
		this.executionId = executionId;
		this.logger = new SimpleJobLogger();
		this.startedAt = new Date();

		this.state = JobState.PRESTART;
	}

	@Override
	public Action getAction() {
		if (jobData == null) {
			return null;
		}
		return jobData.getAction();
	}

	@Override
	public PlatformLayerKey getTargetItemKey() {
		if (jobData == null) {
			return null;
		}
		return jobData.getTargetItemKey();
	}

	@Override
	public ProjectAuthorization getProjectAuthorization() {
		return authentication;
	}

	@Override
	public ServiceType getServiceType() {
		return serviceType;
	}

	@Override
	public void setState(JobState state) {
		this.state = state;
		this.jobExecution.state = state;
	}

	@Override
	public JobLogger getLogger() {
		return logger;
	}

	@Override
	public JobState getState() {
		return state;
	}

	@Override
	public String getExecutionId() {
		return executionId;
	}

	@Override
	public void recordJobEnd() throws OpsException {
		if (jobData == null) {
			return;
		}

		registry.jobFinished(this);
	}

	public Date getStartedAt() {
		return startedAt;
	}

	@Override
	public JobExecutionData getJobExecution() {
		return jobExecution;
	}

}
