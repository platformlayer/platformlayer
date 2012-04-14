package org.platformlayer.ops;

import org.platformlayer.ops.guice.OpsContextProvider;
import org.platformlayer.ops.ssh.ISshContext;
import org.platformlayer.ssh.mina.MinaSshContext;

import com.google.inject.AbstractModule;

public class GuiceOpsConfig extends AbstractModule {
	@Override
	protected void configure() {
		bind(ISshContext.class).to(MinaSshContext.class);

		try {
			OpsConfiguration configuration = new OpsConfiguration();
			bind(OpsConfiguration.class).toInstance(configuration);
		} catch (OpsException e) {
			throw new IllegalStateException("Cannot load system configuration", e);
		}

		bind(OpsSystem.class);

		bind(OpsContext.class).toProvider(OpsContextProvider.class);
	}
}
