package org.platformlayer.ops.pool;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.OpsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DelegatingResourcePool<T> implements ResourcePool<T> {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(DelegatingResourcePool.class);

	final ResourcePool<T> underlying;

	public DelegatingResourcePool(ResourcePool<T> underlying) {
		this.underlying = underlying;
	}

	@Override
	public void release(PlatformLayerKey holder, T item) throws OpsException {
		underlying.release(holder, item);
	}

	// @Override
	// public T read(String id) throws OpsException {
	// return underlying.read(id);
	// }

	@Override
	public T findAssigned(PlatformLayerKey holder) throws OpsException {
		return underlying.findAssigned(holder);
	}

	@Override
	public T assign(PlatformLayerKey owner, boolean required) throws OpsException {
		return underlying.assign(owner, required);
	}

	// @Override
	// public boolean addResource(T item) throws OpsException {
	// return underlying.addResource(item);
	// }

}
