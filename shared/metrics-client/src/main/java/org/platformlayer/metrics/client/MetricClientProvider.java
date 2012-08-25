package org.platformlayer.metrics.client;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.config.Configuration;
import org.platformlayer.crypto.EncryptionStore;
import org.platformlayer.ops.OpsException;

import com.google.inject.Provider;

public class MetricClientProvider implements Provider<MetricClient> {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(MetricClientProvider.class);

	@Inject
	Configuration configuration;

	@Inject
	EncryptionStore encryptionStore;

	@Override
	public MetricClient get() {
		try {
			return MetricClient.build(configuration, encryptionStore);
		} catch (OpsException e) {
			throw new IllegalStateException("Error building metric client", e);
		}
	}
}
