package org.platformlayer.xaas.web.resources;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ids.ManagedItemId;
import org.platformlayer.jobs.model.JobData;
import org.platformlayer.jobs.model.JobDataList;
import org.platformlayer.jobs.model.JobExecutionList;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.tasks.JobRegistry;

import com.google.common.collect.Lists;

public class JobsCollectionResource extends XaasResourceBase {
	@Inject
	JobRegistry jobRegistry;

	@Inject
	Provider<JobResource> jobResourceProvider;

	@GET
	@Path("runs")
	@Produces({ XML, JSON })
	public JobExecutionList getExecutions() throws OpsException {
		JobExecutionList executions = jobRegistry.listExecutions(getProject());
		return executions;
	}

	@Path("{jobId}")
	public JobResource getJob(@PathParam("jobId") String jobId) throws OpsException {
		PlatformLayerKey jobKey = JobData.buildKey(getProject(), new ManagedItemId(jobId));

		JobData record = jobRegistry.getJob(jobKey);
		if (record == null) {
			raiseNotFound();
		}

		JobResource jobResource = jobResourceProvider.get();
		jobResource.init(record);
		return jobResource;
	}

	@GET
	@Produces({ XML, JSON })
	public JobDataList getActiveJobs() {
		List<JobData> jobList = jobRegistry.listJobs(getProject());
		JobDataList jobs = JobDataList.create();
		jobs.jobs = Lists.newArrayList();
		for (JobData jobData : jobList) {
			jobs.jobs.add(jobData);
		}
		return jobs;
	}

}
