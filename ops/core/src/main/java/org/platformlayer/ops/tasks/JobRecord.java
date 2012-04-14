package org.platformlayer.ops.tasks;

import java.util.UUID;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ids.ManagedItemId;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.jobs.model.JobData;
import org.platformlayer.jobs.model.JobLog;
import org.platformlayer.jobs.model.JobState;
import org.platformlayer.ops.OperationType;
import org.platformlayer.ops.auth.OpsAuthentication;

public class JobRecord {
	final JobKey key;
	final OpsAuthentication auth;

	final String jobId;

	final JobLog log;
	final ServiceType serviceType;

	JobState state;

	boolean isDone;

	public JobRecord(PlatformLayerKey itemKey, OperationType operationType, OpsAuthentication auth) {
		this(new JobKey(itemKey, operationType), itemKey.getServiceType(), auth);
	}

	JobRecord(JobKey key, ServiceType serviceType, OpsAuthentication auth) {
		this.key = key;
		this.serviceType = serviceType;
		this.auth = auth;

		this.jobId = UUID.randomUUID().toString();

		this.log = new JobLog();
	}

	public JobRecord(ServiceType serviceType, OpsAuthentication auth) {
		this(null, serviceType, auth);
	}

	public PlatformLayerKey getJobKey() {
		PlatformLayerKey jobKey = JobData.buildKey(auth.getProjectId(), new ManagedItemId(jobId));
		return jobKey;
	}

	public OperationType getOperationType() {
		return key.operationType;
	}

	public JobLog getLog() {
		return log;
	}

	public OpsAuthentication getAuth() {
		return auth;
	}

	public PlatformLayerKey getTargetItemKey() {
		return key.getTargetItemKey();
	}

	public ServiceType getServiceType() {
		return serviceType;
	}

	public JobData getJobData() {
		JobData jobData = new JobData();
		jobData.state = state;

		jobData.key = getJobKey();

		// public String targetId;
		// public Action action;

		return jobData;
	}

	public void setState(JobState state, boolean isDone) {
		this.state = state;
		this.isDone = isDone;
	}

	public JobState getState() {
		return state;
	}

	public boolean willExecute() {
		return !isDone;
	}

}
