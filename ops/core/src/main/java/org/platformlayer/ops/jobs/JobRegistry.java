package org.platformlayer.ops.jobs;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.platformlayer.RepositoryException;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.jobs.model.JobData;
import org.platformlayer.jobs.model.JobLog;
import org.platformlayer.ops.OpsException;
import org.platformlayer.xaas.repository.JobRepository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Singleton
public class JobRegistry {
    private static final int RECENT_JOB_COUNT = 100;

    @Inject
    JobRepository repository;

    final Map<PlatformLayerKey, JobRecord> activeJobs = Maps.newHashMap();

    final LinkedList<JobRecord> recentJobs = Lists.newLinkedList();

    public List<JobRecord> getActiveJobs() {
        List<JobRecord> jobs = Lists.newArrayList();
        synchronized (activeJobs) {
            jobs.addAll(activeJobs.values());
        }
        synchronized (recentJobs) {
            for (JobRecord recentJob : recentJobs) {
                jobs.add(recentJob);
            }
        }
        return jobs;
    }

    public void recordJobEnd(JobRecord record) throws OpsException {
        PlatformLayerKey key = record.data.key;

        synchronized (activeJobs) {
            activeJobs.remove(key);
        }

        synchronized (recentJobs) {
            recentJobs.push(record);
            if (recentJobs.size() > RECENT_JOB_COUNT) {
                recentJobs.pop();
            }
        }

        try {
            repository.recordJob(key, record.itemKey, record.data.state, record.log);
        } catch (RepositoryException e) {
            throw new OpsException("Error writing job to repository", e);
        }
    }

    public JobRecord startJob(PlatformLayerKey jobKey) {
        JobData data = new JobData();
        data.key = jobKey;

        JobRecord record = new JobRecord();
        record.data = data;

        record.log = new JobLog();

        if (jobKey != null) {
            synchronized (activeJobs) {
                activeJobs.put(jobKey, record);
            }
        }

        return record;
    }

    public JobRecord getJob(PlatformLayerKey jobKey, boolean fetchLog) {
        JobRecord jobRecord = null;
        synchronized (activeJobs) {
            jobRecord = activeJobs.get(jobKey);
        }

        if (jobRecord == null) {
            synchronized (recentJobs) {
                for (JobRecord recentJob : recentJobs) {
                    if (recentJob.data.key.equals(jobKey)) {
                        jobRecord = recentJob;
                        break;
                    }
                }
            }
        }

        if (jobRecord == null) {
            throw new UnsupportedOperationException();

            // jobRecord = repository.getJob(jobId, fetchLog);
        }

        return jobRecord;
    }

}
