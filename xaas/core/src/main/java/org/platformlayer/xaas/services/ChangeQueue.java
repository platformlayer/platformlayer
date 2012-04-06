package org.platformlayer.xaas.services;

import org.platformlayer.core.model.ManagedItemState;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.auth.OpsAuthentication;

public interface ChangeQueue {
    void notifyChange(OpsAuthentication auth, PlatformLayerKey itemKey, ManagedItemState newState);
}
