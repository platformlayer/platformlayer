package org.platformlayer.auth.keystone;

import org.platformlayer.auth.JdbcUserRepository;
import org.platformlayer.auth.UserDatabase;
import org.platformlayer.inject.GuiceObjectInjector;
import org.platformlayer.inject.ObjectInjector;

import com.google.inject.AbstractModule;

public class KeystoneOpsUserModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(ObjectInjector.class).to(GuiceObjectInjector.class);

		bind(UserDatabase.class).to(JdbcUserRepository.class).asEagerSingleton();

		bind(KeystoneUserAuthenticator.class).to(KeystoneRepositoryAuthenticator.class).asEagerSingleton();
	}
}
