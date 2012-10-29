package org.platformlayer.xaas.repository;

import org.platformlayer.RepositoryException;
import org.platformlayer.common.JobState;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.jobs.model.JobData;
import org.platformlayer.jobs.model.JobLog;

public interface JobRepository {
	JobData getJob(PlatformLayerKey jobId, boolean fetchLog);

	void recordJob(PlatformLayerKey jobId, PlatformLayerKey itemKey, JobState jobState, JobLog jobLog)
			throws RepositoryException;

	// JobDataList getJobList();
}
