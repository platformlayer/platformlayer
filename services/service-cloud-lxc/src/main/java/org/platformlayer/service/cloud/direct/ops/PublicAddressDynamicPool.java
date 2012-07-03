package org.platformlayer.service.cloud.direct.ops;

import java.io.File;
import java.util.Properties;

import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.pool.PoolBuilder;
import org.platformlayer.ops.pool.StaticFilesystemBackedPool;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

class PublicAddressDynamicPool extends StaticFilesystemBackedPool {
	private final int publicPort;

	public PublicAddressDynamicPool(PoolBuilder poolBuilder, OpsTarget target, File resourceDir, File assignedDir,
			int publicPort) {
		super(poolBuilder, target, resourceDir, assignedDir);
		this.publicPort = publicPort;
	}

	@Override
	protected Iterable<String> pickRandomResource() throws OpsException {
		return Iterables.transform(super.pickRandomResource(), new Function<String, String>() {
			@Override
			public String apply(String input) {
				return input + "_" + publicPort;
			}
		});
	}

	@Override
	public Properties readProperties(String key) throws OpsException {
		String[] tokens = key.split("_");
		if (tokens.length != 2) {
			throw new OpsException("Invalid key format");
		}
		Properties properties = super.readProperties(tokens[0]);
		properties.put("port", tokens[1]);
		return properties;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ":" + resourceDir + ":" + publicPort;
	}
};
