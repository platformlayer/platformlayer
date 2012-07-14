package org.platformlayer.xaas.ops;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.ManagedItemState;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.model.ProjectAuthorization;
import org.platformlayer.ops.OperationType;
import org.platformlayer.ops.tasks.JobRegistry;
import org.platformlayer.xaas.repository.ManagedItemRepository;
import org.platformlayer.xaas.services.ChangeQueue;

public class InProcessChangeQueue implements ChangeQueue {
	static final Logger log = Logger.getLogger(InProcessChangeQueue.class);

	@Inject
	JobRegistry operations;

	@Inject
	ManagedItemRepository repository;

	@Override
	public PlatformLayerKey notifyChange(ProjectAuthorization auth, PlatformLayerKey itemKey, ManagedItemState newState) {
		switch (newState) {
		case CREATION_REQUESTED: {
			return operations.enqueueOperation(OperationType.Configure, auth, itemKey);
		}

		case DELETE_REQUESTED: {
			return operations.enqueueOperation(OperationType.Delete, auth, itemKey);
		}

		default: {
			throw new IllegalStateException("Unknown state for action: " + newState);
		}
		}
	}
}
