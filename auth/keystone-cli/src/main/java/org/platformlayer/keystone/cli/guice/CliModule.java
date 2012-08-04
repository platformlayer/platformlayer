package org.platformlayer.keystone.cli.guice;

import java.util.Properties;

import org.platformlayer.auth.KeystoneJdbcModule;
import org.platformlayer.config.Configuration;
import org.platformlayer.keystone.cli.KeystoneCliOptions;

import com.google.inject.AbstractModule;

public class CliModule extends AbstractModule {

	private final KeystoneCliOptions options;

	public CliModule(KeystoneCliOptions options) {
		this.options = options;
	}

	@Override
	protected void configure() {
		Properties properties = options.getConfigurationProperties();
		Configuration configuration = Configuration.from(properties);

		// Names.bindProperties(binder(), config);

		binder().install(new KeystoneJdbcModule(configuration));
	}

}
