package org.platformlayer.ops.jobs;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.jobs.model.JobData;
import org.platformlayer.jobs.model.JobLog;

public class JobRecord {
    public PlatformLayerKey itemKey;
    public JobLog log;
    public JobData data;
}
