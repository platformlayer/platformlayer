package org.platformlayer.service.cloud.direct.ops.kvm;

import java.io.File;
import java.util.Properties;

import javax.inject.Provider;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.lxc.FilesystemBackedPool;

public class PoolAssignment implements Provider<Properties> {
	public Provider<FilesystemBackedPool> poolProvider;

	public File holder;

	private FilesystemBackedPool cachedPool;

	private Properties assigned;

	private FilesystemBackedPool getPool() {
		if (cachedPool == null) {
			cachedPool = poolProvider.get();
		}
		return cachedPool;
	}

	private OpsTarget getTarget() {
		return OpsContext.get().getInstance(OpsTarget.class);
	}

	@Handler
	public void handler() throws OpsException {
		if (assigned == null) {
			FilesystemBackedPool pool = getPool();
			String key = pool.assign(holder, true);
			assigned = pool.readProperties(key);
		}
	}

	public Properties getAssigned() {
		return assigned;
	}

	@Override
	public Properties get() {
		return getAssigned();
	}
}
