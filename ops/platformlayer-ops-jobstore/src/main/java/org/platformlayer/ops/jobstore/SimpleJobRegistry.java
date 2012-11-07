//package org.platformlayer.ops.jobstore;
//
//import java.util.Collections;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//
//import javax.inject.Inject;
//import javax.inject.Singleton;
//
//import org.platformlayer.RepositoryException;
//import org.platformlayer.core.model.Action;
//import org.platformlayer.core.model.PlatformLayerKey;
//import org.platformlayer.ids.ProjectId;
//import org.platformlayer.jobs.model.JobData;
//import org.platformlayer.jobs.model.JobExecutionData;
//import org.platformlayer.jobs.model.JobLog;
//import org.platformlayer.jobs.model.JobLogLine;
//import org.platformlayer.model.ProjectAuthorization;
//import org.platformlayer.ops.OpsException;
//import org.platformlayer.ops.OpsSystem;
//import org.platformlayer.ops.tasks.JobGraph;
//import org.platformlayer.ops.tasks.JobRecord;
//import org.platformlayer.ops.tasks.OpsContextBuilder;
//import org.platformlayer.xaas.repository.JobRepository;
//
//import com.google.common.base.Objects;
//import com.google.common.collect.Lists;
//import com.google.common.collect.Maps;
//
//@Singleton
//public class SimpleJobRegistry implements JobRegistry {
//	@Inject
//	OpsSystem opsSystem;
//
//	@Inject
//	JobRepository repository;
//
//	final Map<PlatformLayerKey, JobRecord> activeJobs = Maps.newHashMap();
//
//	final LinkedList<JobRecord> recentJobs = Lists.newLinkedList();
//
//	@Inject
//	JobGraph jobGraph;
//
//	@Inject
//	OpsContextBuilder opsContextBuilder;
//
//	public PlatformLayerKey enqueueOperation(Action action, ProjectAuthorization auth, PlatformLayerKey targetItem) {
//
//		ProjectId projectId;
//		try {
//			projectId = opsContextBuilder.getRunAsProjectId(auth);
//		} catch (OpsException e) {
//			throw new IllegalStateException("Error getting projectId", e);
//		}
//
//		JobRecord jobRecord = new JobRecord(targetItem, action, targetItem.getServiceType(), auth, projectId);
//
//		return jobGraph.trigger(jobRecord);
//	}
//
//	private static final int RECENT_JOB_COUNT = 100;
//
//	public List<JobRecord> getActiveJobs(ProjectId projectId) {
//		List<JobRecord> jobs = Lists.newArrayList();
//		synchronized (activeJobs) {
//			for (JobRecord job : activeJobs.values()) {
//				if (!Objects.equal(job.getJobKey().getProject(), projectId)) {
//					continue;
//				}
//				jobs.add(job);
//			}
//		}
//		synchronized (recentJobs) {
//			for (JobRecord job : recentJobs) {
//				if (!job.getJobKey().getProject().equals(projectId)) {
//					continue;
//				}
//				jobs.add(job);
//			}
//		}
//		return jobs;
//	}
//
//	void recordJobEnd(JobRecord record) throws OpsException {
//		PlatformLayerKey key = record.getJobKey();
//
//		synchronized (activeJobs) {
//			activeJobs.remove(key);
//		}
//
//		synchronized (recentJobs) {
//			recentJobs.push(record);
//			if (recentJobs.size() > RECENT_JOB_COUNT) {
//				recentJobs.pop();
//			}
//		}
//
//		try {
//			repository.recordJob(key, record.getTargetItemKey(), record.getState(), record.getLog());
//		} catch (RepositoryException e) {
//			throw new OpsException("Error writing job to repository", e);
//		}
//	}
//
//	JobRecord startJob(JobRecord jobRecord) {
//		PlatformLayerKey jobKey = jobRecord.getJobKey();
//
//		if (jobKey != null) {
//			synchronized (activeJobs) {
//				activeJobs.put(jobKey, jobRecord);
//			}
//		}
//
//		return jobRecord;
//	}
//
//	public JobRecord getJob(PlatformLayerKey jobKey, boolean fetchLog) {
//		JobRecord jobRecord = null;
//		synchronized (activeJobs) {
//			jobRecord = activeJobs.get(jobKey);
//		}
//
//		if (jobRecord == null) {
//			synchronized (recentJobs) {
//				for (JobRecord recentJob : recentJobs) {
//					if (recentJob.getJobKey().equals(jobKey)) {
//						jobRecord = recentJob;
//						break;
//					}
//				}
//			}
//		}
//
//		if (jobRecord == null) {
//			throw new UnsupportedOperationException();
//
//			// jobRecord = repository.getJob(jobId, fetchLog);
//		}
//
//		return jobRecord;
//	}
//
//	public JobLog getJobLog(PlatformLayerKey jobKey, String executionId, int logSkip) {
//		JobData jobData = job.getJobData(fetchLog);
//		if (!fetchLog) {
//			assert jobData.log == null;
//		}
//
//		if (jobData.log != null && logSkip != 0) {
//			List<JobLogLine> lines = jobData.log.lines;
//			jobData.log = new JobLog();
//
//			if (lines == null || lines.size() <= logSkip) {
//				jobData.log.lines = Collections.emptyList();
//			} else {
//				jobData.log.lines = lines.subList(logSkip, lines.size());
//			}
//		}
//	}
//
//	public JobExecutionData findExecution(JobRecord job, String runId) {
//		// TODO Auto-generated method stub
//		throw new UnsupportedOperationException();
//	}
//
//	public List<JobExecutionData> listExecutions(JobRecord job) {
//		// TODO Auto-generated method stub
//		throw new UnsupportedOperationException();
//	}
//
// }