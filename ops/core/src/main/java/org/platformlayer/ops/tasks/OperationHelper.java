package org.platformlayer.ops.tasks;

import java.util.UUID;

import javax.inject.Inject;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ids.ManagedItemId;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.jobs.model.JobData;
import org.platformlayer.ops.OperationType;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.auth.OpsAuthentication;

public class OperationHelper {
    @Inject
    OpsSystem opsSystem;

    @Inject
    OperationQueue operationQueue;

    public PlatformLayerKey enqueueOperation(OperationType operationType, OpsAuthentication auth, PlatformLayerKey itemKey) {
        String jobId = UUID.randomUUID().toString();

        ServiceType serviceType = itemKey.getServiceType();

        PlatformLayerKey jobKey = JobData.buildKey(auth.getProjectId(), new ManagedItemId(jobId));

        OperationWorker operationWorker = new OperationWorker(opsSystem, operationType, serviceType, auth, itemKey, jobKey);
        operationQueue.submit(operationWorker);

        return jobKey;
    }
}
