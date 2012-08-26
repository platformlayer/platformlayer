package org.platformlayer.auth.keystone;

import org.platformlayer.auth.JdbcUserRepository;
import org.platformlayer.auth.UserDatabase;
import org.platformlayer.auth.services.SystemAuthenticator;

import com.google.inject.AbstractModule;

public class KeystoneOpsSystemModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(UserDatabase.class).to(JdbcUserRepository.class).asEagerSingleton();

		bind(KeystoneUserAuthenticator.class).to(KeystoneRepositoryAuthenticator.class).asEagerSingleton();

		bind(SystemAuthenticator.class).to(ClientCertificateSystemAuthenticator.class).asEagerSingleton();
	}
}
