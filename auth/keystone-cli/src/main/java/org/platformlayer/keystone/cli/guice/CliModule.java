package org.platformlayer.keystone.cli.guice;

import java.util.Properties;

import org.platformlayer.keystone.cli.KeystoneCliOptions;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
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

		bindAuthenticationModules(config);
	}

	private void bindAuthenticationModules(Properties config) {
		String userProvider = config.getProperty("auth.user.module");
		if (userProvider != null) {
			installModule(userProvider);
		}
		String systemProvider = config.getProperty("auth.system.module");
		if (systemProvider != null) {
			if (!systemProvider.equals(userProvider)) {
				installModule(systemProvider);
			}
		}
	}

	private void installModule(String moduleClassName) {
		try {
			Class<?> moduleClass = Class.forName(moduleClassName);
			Module module = (Module) moduleClass.newInstance();
			binder().install(module);
		} catch (Exception e) {
			throw new IllegalStateException("Error loading class: " + moduleClassName);
		}
	}

}
