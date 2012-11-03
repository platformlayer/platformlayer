//package org.platformlayer.ops.jobstore;
//
//import java.util.List;
//import java.util.concurrent.ConcurrentMap;
//
//import javax.inject.Inject;
//import javax.inject.Singleton;
//
//import org.platformlayer.core.model.Action;
//import org.platformlayer.core.model.PlatformLayerKey;
//import org.platformlayer.jobs.model.JobData;
//import org.platformlayer.ops.OpsSystem;
//import org.platformlayer.ops.tasks.OperationQueue;
//
//import com.google.common.collect.Lists;
//import com.google.common.collect.Maps;
//
//@Singleton
//public class JobGraph {
//	@Inject
//	OpsSystem opsSystem;
//
//	@Inject
//	OperationQueue operationQueue;
//
//	final ConcurrentMap<PlatformLayerKey, ItemJobs> itemMap = Maps.newConcurrentMap();
//
//	static class ItemJobs {
//		final List<JobRecord> jobs = Lists.newArrayList();
//	}
//
//	 public ActiveJobExecution trigger(JobData jobData) {
//	 ItemJobs jobs = getItemJobs(jobData);
//	
//	 synchronized (jobs) {
//	 for (JobRecord existing : jobs.jobs) {
//	 if (!isSameJob(existing.action, jobData.getAction()) {
//	 continue;
//	 }
//	
//	 if (existing.willExecute()) {
//	 return existing;
//	 } else {
//	 jobs.jobs.remove(existing);
//	 break;
//	 }
//	 }
//	
//	 jobs.jobs.add(jobRecord);
//	
//	 JobRegistry jobRegistry = opsSystem.getJobRegistry();
//	 ActiveJobExecution activeJob = jobRegistry.startJob(jobData);
//	
//	 OperationWorker operationWorker = new OperationWorker(opsSystem, activeJob);
//	 operationQueue.submit(operationWorker);
//	 return activeJob;
//	 }
//	 }
//
//	private static boolean isSameJob(Action a, Action b) {
//		if (a == null) {
//			return b == null;
//		}
//		if (b == null) {
//			return a == null;
//		}
//
//		if (a.getClass() != b.getClass()) {
//			return false;
//		}
//
//		// TODO: Do we need any further consideration??
//		return true;
//	}
//
//	private ItemJobs getItemJobs(JobData jobData) {
//		PlatformLayerKey targetItemKey = jobData.getTargetItemKey();
//		if (targetItemKey == null) {
//			throw new UnsupportedOperationException();
//		}
//
//		ItemJobs itemJobs = itemMap.get(targetItemKey);
//		if (itemJobs == null) {
//			itemJobs = new ItemJobs();
//			ItemJobs existing = itemMap.putIfAbsent(targetItemKey, itemJobs);
//			if (existing != null) {
//				return existing;
//			}
//		}
//		return itemJobs;
//	}
// }
