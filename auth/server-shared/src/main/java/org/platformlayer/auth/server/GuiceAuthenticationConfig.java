package org.platformlayer.auth.server;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.openstack.crypto.KeyStoreUtils;
import org.openstack.utils.PropertyUtils;
import org.platformlayer.auth.services.CacheSystem;
import org.platformlayer.auth.services.TokenService;
import org.platformlayer.auth.services.crypto.SharedSecretTokenService;
import org.platformlayer.auth.services.memory.SimpleCacheSystem;
import org.platformlayer.crypto.EncryptionStore;
import org.platformlayer.crypto.KeyStoreEncryptionStore;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.name.Names;

public class GuiceAuthenticationConfig extends AbstractModule {

	@Override
	protected void configure() {
		String configFilePath = System.getProperty("conf");
		if (configFilePath == null) {
			configFilePath = new File(new File("."), "configuration.properties").getAbsolutePath();
		}

		File configFile = new File(configFilePath);
		File baseDir = configFile.getParentFile();
		Properties config;

		try {
			config = PropertyUtils.loadProperties(configFile);
		} catch (IOException e) {
			throw new IllegalStateException("Error loading configuration file: " + configFilePath, e);
		}

		String secret = config.getProperty("sharedsecret");
		if (secret == null) {
			throw new IllegalStateException("sharedsecret is required");
		}

		Names.bindProperties(binder(), config);

		bindEncryptionStore(config);

		bindAuthenticationModules(config);

		TokenService tokenService = new SharedSecretTokenService(secret);
		bind(TokenService.class).toInstance(tokenService);

		int cacheSize = 1000;
		CacheSystem simpleCacheSystem = new SimpleCacheSystem(cacheSize);
		bind(CacheSystem.class).toInstance(simpleCacheSystem);
	}

	private EncryptionStore bindEncryptionStore(Properties configuration) {
		String keystorePath = configuration.getProperty("keystore", "keystore.jks");
		String secret = configuration.getProperty("keystore.password", KeyStoreUtils.DEFAULT_KEYSTORE_SECRET);

		File keystoreFile = new File(keystorePath);

		EncryptionStore encryptionStore = KeyStoreEncryptionStore.build(keystoreFile, secret);
		bind(EncryptionStore.class).toInstance(encryptionStore);

		return encryptionStore;
	}

	private void bindAuthenticationModules(Properties config) {
		String userProvider = config.getProperty("auth.user.module");
		if (userProvider != null) {
			installModule(userProvider);
		}
		String systemProvider = config.getProperty("auth.system.module");
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
