package org.platformlayer.ops.pool;

import java.io.File;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.platformlayer.ops.OpsException;

public class DelegatingResourcePool implements ResourcePool {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(DelegatingResourcePool.class);

	final ResourcePool underlying;

	@Override
	public void release(File holder, String key) throws OpsException {
		underlying.release(holder, key);
	}

	@Override
	public Properties readProperties(String key) throws OpsException {
		return underlying.readProperties(key);
	}

	@Override
	public String findAssigned(File holder) throws OpsException {
		return underlying.findAssigned(holder);
	}

	@Override
	public String assign(File owner, boolean required) throws OpsException {
		return underlying.assign(owner, required);
	}

	public DelegatingResourcePool(ResourcePool underlying) {
		super();
		this.underlying = underlying;
	}

}
