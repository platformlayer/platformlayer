package org.platformlayer.ops.tasks;

import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.OpsSystem;

import com.google.common.collect.Maps;

@Singleton
public class JobGraph {
	@Inject
	OpsSystem opsSystem;

	@Inject
	OperationQueue operationQueue;

	final ConcurrentMap<JobKey, JobRecord> jobs = Maps.newConcurrentMap();

	public PlatformLayerKey trigger(JobRecord jobRecord) {
		JobKey key = jobRecord.key;

		while (true) {
			JobRecord existing = jobs.putIfAbsent(key, jobRecord);
			if (existing == null) {
				break;

			}
			if (existing.willExecute()) {
				return existing.getJobKey();
			}

			if (jobs.replace(key, existing, jobRecord)) {
				break;
			}
		}

		OperationWorker operationWorker = new OperationWorker(opsSystem, jobRecord);
		operationQueue.submit(operationWorker);
		return jobRecord.getJobKey();
	}
}
