package org.platformlayer.ops.pool;

import javax.inject.Provider;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsProvider;

public abstract class PoolAssignment<T> implements Provider<T> {
	public OpsProvider<ResourcePool<T>> poolProvider;

	public PlatformLayerKey holder;

	private ResourcePool<T> cachedPool;

	private T assignedResource;

	private ResourcePool<T> getPool() throws OpsException {
		if (cachedPool == null) {
			cachedPool = poolProvider.get();
		}
		return cachedPool;
	}

	@Handler
	public void handler() throws OpsException {
		ResourcePool<T> pool = getPool();

		if (OpsContext.isConfigure()) {
			if (assignedResource == null) {
				assignedResource = pool.assign(holder, true);
			}
		}

		if (OpsContext.isDelete()) {
			T assigned = pool.findAssigned(holder);
			if (assigned != null) {
				pool.release(holder, assigned);
				assignedResource = null;
			}
		}
	}

	@Override
	public T get() {
		return assignedResource;
	}
}
