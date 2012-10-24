package org.platformlayer.ops;

public interface OpsTargetOperation<T> {

	T apply(OpsTarget target) throws OpsException;

	boolean isCacheable();

}
