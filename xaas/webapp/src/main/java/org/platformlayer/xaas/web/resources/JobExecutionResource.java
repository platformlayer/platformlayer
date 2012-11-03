package org.platformlayer.xaas.web.resources;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.platformlayer.jobs.model.JobExecutionData;
import org.platformlayer.jobs.model.JobLog;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.tasks.JobRegistry;

public class JobExecutionResource extends XaasResourceBase {
	@Inject
	JobRegistry jobRegistry;

	JobExecutionData jobExecution;

	public void init(JobExecutionData jobExecution) {
		if (this.jobExecution != null) {
			throw new IllegalStateException();
		}
		this.jobExecution = jobExecution;
	}

	@GET
	@Produces({ XML, JSON })
	public JobExecutionData get() {
		return jobExecution;
	}

	@GET
	@Path("log")
	@Produces({ XML, JSON })
	public JobLog getLog(@QueryParam("log.skip") int logSkip) throws OpsException {
		JobLog log = jobRegistry.getJobLog(jobExecution.getJobKey(), jobExecution.executionId, logSkip);
		if (log == null) {
			raiseNotFound();
		}

		return log;
	}

}
