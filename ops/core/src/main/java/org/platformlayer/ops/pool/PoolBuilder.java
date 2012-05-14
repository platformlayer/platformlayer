package org.platformlayer.ops.pool;

import org.platformlayer.ops.OpsException;

public interface PoolBuilder {

	int extendPool(FilesystemBackedPool filesystemBackedPool) throws OpsException;

}
