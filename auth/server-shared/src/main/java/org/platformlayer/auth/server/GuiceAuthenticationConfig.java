package org.platformlayer.auth.server;

import org.platformlayer.auth.services.CacheSystem;
import org.platformlayer.auth.services.TokenService;
import org.platformlayer.auth.services.crypto.SharedSecretTokenService;
import org.platformlayer.auth.services.memory.SimpleCacheSystem;
import org.platformlayer.crypto.EncryptionStoreProvider;

import com.fathomdb.crypto.EncryptionStore;
import com.google.inject.AbstractModule;

public class GuiceAuthenticationConfig extends AbstractModule {

	@Override
	protected void configure() {
		bind(EncryptionStore.class).toProvider(EncryptionStoreProvider.class);

		bind(TokenService.class).toProvider(SharedSecretTokenService.Provider.class).asEagerSingleton();

		int cacheSize = 1000;
		CacheSystem simpleCacheSystem = new SimpleCacheSystem(cacheSize);
		bind(CacheSystem.class).toInstance(simpleCacheSystem);
	}

}
