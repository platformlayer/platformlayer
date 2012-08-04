package org.platformlayer.auth.keystone;

import org.platformlayer.auth.JdbcUserRepository;
import org.platformlayer.auth.KeystoneJdbcModule;
import org.platformlayer.auth.UserDatabase;
import org.platformlayer.config.Configuration;

import com.google.inject.AbstractModule;

public class KeystoneOpsUserModule extends AbstractModule {

	@Override
	protected void configure() {
		Configuration configuration = Configuration.load();
		bind(Configuration.class).toInstance(configuration);

		configuration.bindProperties(binder());

		install(new KeystoneJdbcModule(configuration));

		bind(UserDatabase.class).to(JdbcUserRepository.class).asEagerSingleton();

		bind(KeystoneUserAuthenticator.class).to(KeystoneRepositoryAuthenticator.class).asEagerSingleton();
	}
}
