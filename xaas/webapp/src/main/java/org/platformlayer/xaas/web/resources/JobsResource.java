package org.platformlayer.xaas.web.resources;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ids.ManagedItemId;
import org.platformlayer.jobs.model.JobData;
import org.platformlayer.jobs.model.JobDataList;
import org.platformlayer.jobs.model.JobLog;
import org.platformlayer.ops.jobs.JobRecord;
import org.platformlayer.ops.jobs.JobRegistry;

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

    @Path("{jobId}/log")
    @GET
    @Produces({ XML, JSON })
    public JobLog getJobLog(@PathParam("jobId") String jobId) {
        boolean fetchLog = true;

        PlatformLayerKey jobKey = JobData.buildKey(getProject(), new ManagedItemId(jobId));

        JobRecord job = jobRegistry.getJob(jobKey, fetchLog);
        return job.log;
    }

    @GET
    @Produces({ XML, JSON })
    public JobDataList getJobDataList() {
        List<JobRecord> jobList = jobRegistry.getActiveJobs();
        JobDataList jobs = new JobDataList();
        jobs.jobs = Lists.newArrayList();
        for (JobRecord record : jobList) {
            jobs.jobs.add(record.data);
        }
        return jobs;
    }

}
