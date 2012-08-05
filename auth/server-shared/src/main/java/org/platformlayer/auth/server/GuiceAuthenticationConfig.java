package org.platformlayer.auth.server;

import org.platformlayer.auth.services.CacheSystem;
import org.platformlayer.auth.services.TokenService;
import org.platformlayer.auth.services.crypto.SharedSecretTokenService;
import org.platformlayer.auth.services.memory.SimpleCacheSystem;
import org.platformlayer.config.Configuration;
import org.platformlayer.crypto.EncryptionStore;
import org.platformlayer.crypto.KeyStoreEncryptionStore;
import org.platformlayer.ops.OpsException;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

public class GuiceAuthenticationConfig extends AbstractModule {

	@Override
	protected void configure() {
		try {
			Configuration configuration = Configuration.load();

			String secret = configuration.get("sharedsecret");
			if (secret == null) {
				throw new IllegalStateException("sharedsecret is required");
			}

			// Names.bindProperties(binder(), config);

			EncryptionStore encryptionStore = KeyStoreEncryptionStore.build(configuration);
			bind(EncryptionStore.class).toInstance(encryptionStore);

			bindAuthenticationModules(configuration);

			TokenService tokenService = new SharedSecretTokenService(secret);
			bind(TokenService.class).toInstance(tokenService);

			int cacheSize = 1000;
			CacheSystem simpleCacheSystem = new SimpleCacheSystem(cacheSize);
			bind(CacheSystem.class).toInstance(simpleCacheSystem);
		} catch (OpsException e) {
			throw new IllegalStateException("Error during initialization", e);
		}
	}

	private void bindAuthenticationModules(Configuration config) {
		String userProvider = config.find("auth.user.module");
		if (userProvider != null) {
			installModule(userProvider);
		}
		String systemProvider = config.find("auth.system.module");
		if (systemProvider != null) {
			installModule(systemProvider);
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
