package org.platformlayer.keystone.cli.guice;

import java.util.Properties;

import org.platformlayer.auth.KeystoneJdbcModule;
import org.platformlayer.keystone.cli.KeystoneCliOptions;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class CliModule extends AbstractModule {

	private final KeystoneCliOptions options;

	public CliModule(KeystoneCliOptions options) {
		this.options = options;
	}

	@Override
	protected void configure() {
		Properties config = options.getConfigurationProperties();
		Names.bindProperties(binder(), config);

		binder().install(new KeystoneJdbcModule());
	}

}
