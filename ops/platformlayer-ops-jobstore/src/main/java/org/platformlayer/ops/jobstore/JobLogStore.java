package org.platformlayer.ops.jobstore;

import java.io.IOException;
import java.util.Date;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.jobs.model.JobLog;
import org.platformlayer.ops.log.JobLogger;

public interface JobLogStore {
	JobLog getJobLog(Date startTime, PlatformLayerKey jobKey, String executionId, String cookie, int logSkip)
			throws IOException;

	String saveJobLog(PlatformLayerKey jobKey, String executionId, Date startTime, JobLogger logger) throws IOException;
}