package org.platformlayer.ops.pool;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.OpsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TransformingResourcePool<T, U> implements ResourcePool<U> {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(TransformingResourcePool.class);

	final ResourcePool<T> underlying;

	public TransformingResourcePool(ResourcePool<T> underlying) {
		this.underlying = underlying;
	}

	protected abstract U transform(T item);

	protected abstract T reverse(U item);

	@Override
	public void release(PlatformLayerKey holder, U item) throws OpsException {
		underlying.release(holder, reverse(item));
	}

	// @Override
	// public T read(String id) throws OpsException {
	// return underlying.read(id);
	// }

	@Override
	public U findAssigned(PlatformLayerKey holder) throws OpsException {
		return transform(underlying.findAssigned(holder));
	}

	@Override
	public U assign(PlatformLayerKey owner, boolean required) throws OpsException {
		return transform(underlying.assign(owner, required));
	}

	// @Override
	// public boolean addResource(U item) throws OpsException {
	// return underlying.addResource(reverse(item));
	// }

}
