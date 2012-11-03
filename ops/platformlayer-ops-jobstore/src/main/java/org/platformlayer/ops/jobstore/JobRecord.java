//package org.platformlayer.ops.jobstore;
//
//import java.util.Date;
//import java.util.UUID;
//
//import org.platformlayer.common.JobState;
//import org.platformlayer.core.model.Action;
//import org.platformlayer.core.model.PlatformLayerKey;
//import org.platformlayer.ids.ManagedItemId;
//import org.platformlayer.ids.ProjectId;
//import org.platformlayer.ids.ServiceType;
//import org.platformlayer.jobs.model.JobData;
//import org.platformlayer.jobs.model.JobLog;
//import org.platformlayer.model.ProjectAuthorization;
//
//public class JobRecord {
//	final ProjectAuthorization auth;
//
//	final String jobId;
//
//	final JobLog log;
//	final ServiceType serviceType;
//
//	JobState state;
//
//	private final ProjectId jobProjectId;
//
//	private Date startedAt;
//	private Date endedAt;
//
//	boolean isDone;
//
//	final PlatformLayerKey targetItemKey;
//
//	final Action action;
//
//	public JobRecord(PlatformLayerKey targetItemKey, Action action, ProjectAuthorization auth, ProjectId jobProjectId) {
//		this(targetItemKey, action, targetItemKey.getServiceType(), auth, jobProjectId);
//	}
//
//	JobRecord(PlatformLayerKey targetItemKey, Action action, ServiceType serviceType, ProjectAuthorization auth,
//			ProjectId jobProjectId) {
//		this.targetItemKey = targetItemKey;
//		this.action = action;
//
//		this.serviceType = serviceType;
//		this.auth = auth;
//		this.jobProjectId = jobProjectId;
//
//		this.jobId = UUID.randomUUID().toString();
//
//		this.log = new JobLog();
//	}
//
//	public JobRecord(ServiceType serviceType, ProjectAuthorization auth, ProjectId jobProjectId) {
//		this(null, null, serviceType, auth, jobProjectId);
//	}
//
//	public PlatformLayerKey getJobKey() {
//		PlatformLayerKey jobKey = JobData.buildKey(jobProjectId, new ManagedItemId(jobId));
//		return jobKey;
//	}
//
//	// public OperationType getOperationType() {
//	// if (key == null) {
//	// return null;
//	// }
//	// return key.operationType;
//	// }
//
//	public JobLog getLog() {
//		return log;
//	}
//
//	public ProjectAuthorization getProjectAuthorization() {
//		return auth;
//	}
//
//	public PlatformLayerKey getTargetItemKey() {
//		return targetItemKey;
//	}
//
//	public ServiceType getServiceType() {
//		return serviceType;
//	}
//
//	public JobData getJobData() {
//		JobData jobData = new JobData();
//		jobData.state = state;
//
//		jobData.key = getJobKey();
//
//		jobData.startedAt = this.startedAt;
//		jobData.endedAt = this.endedAt;
//		jobData.action = action;
//		jobData.targetId = targetItemKey;
//
//		if (getLog) {
//			jobData.log = this.log;
//		}
//
//		return jobData;
//	}
//
//	public void setState(JobState state, boolean isDone) {
//		this.state = state;
//		this.isDone = isDone;
//
//		Date now = new Date();
//		switch (state) {
//		case RUNNING:
//			this.startedAt = now;
//			this.endedAt = null;
//			break;
//
//		case FAILED:
//		case SUCCESS:
//			this.endedAt = now;
//			break;
//
//		}
//	}
//
//	public JobState getState() {
//		return state;
//	}
//
//	public boolean willExecute() {
//		return !isDone;
//	}
//
//	public Action getAction() {
//		return action;
//	}
//
// }
