package org.platformlayer.xaas.web.resources;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.platformlayer.jobs.model.JobData;
import org.platformlayer.jobs.model.JobExecutionData;
import org.platformlayer.jobs.model.JobExecutionList;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.tasks.JobRegistry;

public class JobResource extends XaasResourceBase {
	@Inject
	JobRegistry jobRegistry;

	JobData job;

	@Inject
	Provider<JobExecutionResource> jobExecutionResourceProvider;

	public void init(JobData job) {
		if (this.job != null) {
			throw new IllegalStateException();
		}
		this.job = job;
	}

	@GET
	@Path("runs")
	@Produces({ XML, JSON })
	public JobExecutionList getRuns() throws OpsException {
		List<JobExecutionData> jobList = jobRegistry.listExecutions(job.getJobKey());
		JobExecutionList runs = JobExecutionList.create(jobList);
		return runs;
	}

	@GET
	@Produces({ XML, JSON })
	public JobData getRun() throws OpsException {
		return job;
	}

	@Path("runs/{runId}")
	public JobExecutionResource getRun(@PathParam("runId") String runId) throws OpsException {
		JobExecutionData jobExecution = jobRegistry.findExecution(job.getJobKey(), runId);
		if (jobExecution == null) {
			raiseNotFound();
		}

		JobExecutionResource jobExecutionResource = jobExecutionResourceProvider.get();
		jobExecutionResource.init(jobExecution);
		return jobExecutionResource;
	}

}
