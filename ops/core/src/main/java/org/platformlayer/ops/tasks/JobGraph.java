package org.platformlayer.ops.tasks;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.platformlayer.core.model.Action;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.OpsSystem;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Singleton
public class JobGraph {
	@Inject
	OpsSystem opsSystem;

	@Inject
	OperationQueue operationQueue;

	final ConcurrentMap<PlatformLayerKey, ItemJobs> itemMap = Maps.newConcurrentMap();

	static class ItemJobs {
		final List<JobRecord> jobs = Lists.newArrayList();
	}

	public PlatformLayerKey trigger(JobRecord jobRecord) {
		ItemJobs jobs = getItemJobs(jobRecord);

		synchronized (jobs) {
			for (JobRecord existing : jobs.jobs) {
				if (!isSameJob(existing.action, jobRecord.action)) {
					continue;
				}

				if (existing.willExecute()) {
					return existing.getJobKey();
				} else {
					jobs.jobs.remove(existing);
					break;
				}
			}

			jobs.jobs.add(jobRecord);

			OperationWorker operationWorker = new OperationWorker(opsSystem, jobRecord);
			operationQueue.submit(operationWorker);
			return jobRecord.getJobKey();
		}
	}

	private static boolean isSameJob(Action a, Action b) {
		if (a == null) {
			return b == null;
		}
		if (b == null) {
			return a == null;
		}

		if (a.getClass() != b.getClass()) {
			return false;
		}

		// TODO: Do we need any further consideration??
		return true;
	}

	private ItemJobs getItemJobs(JobRecord jobRecord) {
		PlatformLayerKey targetItemKey = jobRecord.getTargetItemKey();
		if (targetItemKey == null) {
			throw new UnsupportedOperationException();
		}

		ItemJobs itemJobs = itemMap.get(targetItemKey);
		if (itemJobs == null) {
			itemJobs = new ItemJobs();
			ItemJobs existing = itemMap.putIfAbsent(targetItemKey, itemJobs);
			if (existing != null) {
				return existing;
			}
		}
		return itemJobs;
	}
}
