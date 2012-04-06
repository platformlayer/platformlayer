package org.platformlayer.xaas.ops;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.ManagedItemState;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.OperationType;
import org.platformlayer.ops.auth.OpsAuthentication;
import org.platformlayer.ops.tasks.OperationHelper;
import org.platformlayer.xaas.repository.ManagedItemRepository;
import org.platformlayer.xaas.services.ChangeQueue;

public class InProcessChangeQueue implements ChangeQueue {
    static final Logger log = Logger.getLogger(InProcessChangeQueue.class);

    @Inject
    OperationHelper operations;

    @Inject
    ManagedItemRepository repository;

    @Override
    public void notifyChange(OpsAuthentication auth, PlatformLayerKey itemKey, ManagedItemState newState) {
        switch (newState) {
        case CREATION_REQUESTED: {
            operations.enqueueOperation(OperationType.Configure, auth, itemKey);
            break;
        }

        case DELETE_REQUESTED: {
            operations.enqueueOperation(OperationType.Delete, auth, itemKey);
            break;
        }

        default: {
            throw new IllegalStateException("Unknown state for action: " + newState);
        }
        }
    }
}
