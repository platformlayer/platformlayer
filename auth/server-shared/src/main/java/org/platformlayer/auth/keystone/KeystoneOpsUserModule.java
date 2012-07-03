package org.platformlayer.auth.keystone;

import org.platformlayer.auth.JdbcUserRepository;
import org.platformlayer.auth.KeystoneJdbcModule;
import org.platformlayer.auth.UserDatabase;

import com.google.inject.AbstractModule;

public class KeystoneOpsUserModule extends AbstractModule {

	@Override
	protected void configure() {
		install(new KeystoneJdbcModule());

		bind(UserDatabase.class).to(JdbcUserRepository.class).asEagerSingleton();

		bind(KeystoneUserAuthenticator.class).to(KeystoneOpsAuthenticator.class).asEagerSingleton();
	}
}
