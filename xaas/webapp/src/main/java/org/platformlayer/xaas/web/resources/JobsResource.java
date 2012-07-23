package org.platformlayer.xaas.web.resources;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ids.ManagedItemId;
import org.platformlayer.jobs.model.JobData;
import org.platformlayer.jobs.model.JobDataList;
import org.platformlayer.jobs.model.JobLog;
import org.platformlayer.jobs.model.JobLogLine;
import org.platformlayer.ops.tasks.JobRecord;
import org.platformlayer.ops.tasks.JobRegistry;

import com.google.common.collect.Lists;

public class JobsResource extends XaasResourceBase {
	@Inject
	JobRegistry jobRegistry;

	// @Path("{jobId}/data")
	// @Produces({ APPLICATION_XML, APPLICATION_JSON })
	// public JobData getJob(@PathParam("jobId") String jobId) {
	// boolean fetchLog = true;
	// JobData job = jobRegistry.getJob(jobId, fetchLog);
	// return job;
	// }

	@Path("{jobId}")
	@GET
	@Produces({ XML, JSON })
	public JobData getJob(@PathParam("jobId") String jobId, @QueryParam("tree") String tree,
			@QueryParam("log.skip") int logSkip) {
		PlatformLayerKey jobKey = JobData.buildKey(getProject(), new ManagedItemId(jobId));

		boolean fetchLog = true;
		if (tree != null) {
			tree = "," + tree + ",";
			fetchLog = tree.contains(",log,");
		}

		JobRecord record = jobRegistry.getJob(jobKey, fetchLog);
		if (record == null) {
			raiseNotFound();
		}

		JobData jobData = record.getJobData();

		if (!fetchLog) {
			jobData.log = null;
		}

		if (jobData.log != null && logSkip != 0) {
			List<JobLogLine> lines = jobData.log.lines;
			jobData.log = new JobLog();

			if (lines == null || lines.size() <= logSkip) {
				jobData.log.lines = Collections.emptyList();
			} else {
				jobData.log.lines = lines.subList(logSkip, lines.size());
			}
		}

		return jobData;
	}

	@GET
	@Produces({ XML, JSON })
	public JobDataList getJobDataList() {
		List<JobRecord> jobList = jobRegistry.getActiveJobs(getProject());
		JobDataList jobs = new JobDataList();
		jobs.jobs = Lists.newArrayList();
		for (JobRecord record : jobList) {
			jobs.jobs.add(record.getJobData());
		}
		return jobs;
	}

}
