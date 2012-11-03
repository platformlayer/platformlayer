//package org.platformlayer.ops.jobstore;
//
//import java.util.Collections;
//import java.util.List;
//
//import org.platformlayer.core.model.PlatformLayerKey;
//import org.platformlayer.jobs.model.JobLog;
//import org.platformlayer.jobs.model.JobLogLine;
//
//public class InMemoryJobLogStore extends JobLogStore {
//
//	@Override
//	public JobLog getJobLog(PlatformLayerKey jobKey, String executionId, int logSkip) {
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
// }
