package org.platformlayer.xaas.web.resources;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.platformlayer.jobs.model.JobData;
import org.platformlayer.jobs.model.JobExecutionData;
import org.platformlayer.jobs.model.JobLog;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.tasks.JobRegistry;

public class JobExecutionResource extends XaasResourceBase {
	@Inject
	JobRegistry jobRegistry;

	// JobExecutionData jobExecution;

	private JobData job;

	private String runId;

	// public void init(JobExecutionData jobExecution) {
	// if (this.jobExecution != null) {
	// throw new IllegalStateException();
	// }
	// this.jobExecution = jobExecution;
	// }

	@GET
	@Produces({ XML, JSON })
	public JobExecutionData get() throws OpsException {
		JobExecutionData jobExecution = jobRegistry.findExecution(job.getJobKey(), runId);
		if (jobExecution == null) {
			raiseNotFound();
		}

		return jobExecution;
	}

	@GET
	@Path("log")
	@Produces({ XML, JSON })
	public JobLog getLog(@QueryParam("log.skip") int logSkip) throws OpsException {
		JobLog log = jobRegistry.getJobLog(job.getJobKey(), runId, logSkip);
		if (log == null) {
			raiseNotFound();
		}

		return log;
	}

	void init(JobData job, String runId) {
		if (this.job != null) {
			throw new IllegalStateException();
		}

		this.job = job;
		this.runId = runId;

	}

}
