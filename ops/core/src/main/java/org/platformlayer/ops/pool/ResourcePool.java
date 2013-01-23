package org.platformlayer.ops.pool;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.OpsException;

public interface ResourcePool<T> {

	// boolean addResource(T item) throws OpsException;

	T assign(PlatformLayerKey owner, boolean required) throws OpsException;

	T findAssigned(PlatformLayerKey holder) throws OpsException;

	void release(PlatformLayerKey holder, T item) throws OpsException;

}
