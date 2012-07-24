package org.platformlayer.ops.tasks;

import java.util.Date;
import java.util.UUID;

import org.platformlayer.core.model.Action;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ids.ManagedItemId;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.jobs.model.JobData;
import org.platformlayer.jobs.model.JobLog;
import org.platformlayer.jobs.model.JobState;
import org.platformlayer.model.ProjectAuthorization;
import org.platformlayer.ops.OperationType;

public class JobRecord {
	final JobKey key;
	final ProjectAuthorization auth;

	final String jobId;

	final JobLog log;
	final ServiceType serviceType;

	JobState state;

	private final ProjectId jobProjectId;

	private Date startedAt;
	private Date endedAt;

	boolean isDone;

	public JobRecord(PlatformLayerKey targetItemKey, OperationType operationType, ProjectAuthorization auth,
			ProjectId jobProjectId) {
		this(new JobKey(targetItemKey, operationType), targetItemKey.getServiceType(), auth, jobProjectId);
	}

	JobRecord(JobKey key, ServiceType serviceType, ProjectAuthorization auth, ProjectId jobProjectId) {
		this.key = key;
		this.serviceType = serviceType;
		this.auth = auth;
		this.jobProjectId = jobProjectId;

		this.jobId = UUID.randomUUID().toString();

		this.log = new JobLog();
	}

	public JobRecord(ServiceType serviceType, ProjectAuthorization auth, ProjectId jobProjectId) {
		this(null, serviceType, auth, jobProjectId);
	}

	public PlatformLayerKey getJobKey() {
		PlatformLayerKey jobKey = JobData.buildKey(jobProjectId, new ManagedItemId(jobId));
		return jobKey;
	}

	public OperationType getOperationType() {
		if (key == null) {
			return null;
		}
		return key.operationType;
	}

	public JobLog getLog() {
		return log;
	}

	public ProjectAuthorization getProjectAuthorization() {
		return auth;
	}

	public PlatformLayerKey getTargetItemKey() {
		return key.getTargetItemKey();
	}

	public ServiceType getServiceType() {
		return serviceType;
	}

	public JobData getJobData(boolean getLog) {
		JobData jobData = new JobData();
		jobData.state = state;

		jobData.key = getJobKey();

		jobData.startedAt = this.startedAt;
		jobData.endedAt = this.endedAt;

		OperationType operationType = getOperationType();
		if (operationType != null) {
			// TODO: We'll need to store the action when there's more than just an operationType
			Action action = new Action();
			action.name = operationType.toString();
			jobData.action = action;
		}

		if (key != null) {
			jobData.targetId = key.getTargetItemKey();
		}

		if (getLog) {
			jobData.log = this.log;
		}

		return jobData;
	}

	public void setState(JobState state, boolean isDone) {
		this.state = state;
		this.isDone = isDone;

		Date now = new Date();
		switch (state) {
		case RUNNING:
			this.startedAt = now;
			this.endedAt = null;
			break;

		case FAILED:
		case SUCCESS:
			this.endedAt = now;
			break;

		}
	}

	public JobState getState() {
		return state;
	}

	public boolean willExecute() {
		return !isDone;
	}

}
