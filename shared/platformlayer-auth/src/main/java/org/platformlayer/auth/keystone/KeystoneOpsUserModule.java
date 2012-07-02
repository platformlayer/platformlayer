package org.platformlayer.auth.keystone;

import javax.sql.DataSource;

import org.openstack.keystone.services.SystemAuthenticator;
import org.openstack.keystone.services.UserAuthenticator;
import org.platformlayer.auth.JdbcUserRepository;
import org.platformlayer.auth.OpsProject;
import org.platformlayer.auth.OpsUser;
import org.platformlayer.auth.UserRepository;
import org.platformlayer.guice.GuiceDataSourceProvider;
import org.platformlayer.guice.JdbcGuiceModule;
import org.platformlayer.jdbc.simplejpa.ResultSetMappers;
import org.platformlayer.jdbc.simplejpa.ResultSetMappersProvider;

import com.google.inject.AbstractModule;

public class KeystoneOpsUserModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(DataSource.class).toProvider(new GuiceDataSourceProvider("auth.jdbc.", null));

		JdbcGuiceModule jdbcGuiceModule = new JdbcGuiceModule();
		binder().install(jdbcGuiceModule);

		bind(UserRepository.class).to(JdbcUserRepository.class).asEagerSingleton();
		bind(UserAuthenticator.class).to(KeystoneOpsAuthenticator.class).asEagerSingleton();

		bind(SystemAuthenticator.class).to(ClientCertificateSystemAuthenticator.class).asEagerSingleton();

		bind(ResultSetMappers.class).toProvider(ResultSetMappersProvider.build(OpsUser.class, OpsProject.class));
	}
}
