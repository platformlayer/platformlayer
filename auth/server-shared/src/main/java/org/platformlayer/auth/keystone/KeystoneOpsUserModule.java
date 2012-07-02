package org.platformlayer.auth.keystone;

import javax.sql.DataSource;

import org.openstack.keystone.services.SystemAuthenticator;
import org.platformlayer.auth.JdbcUserRepository;
import org.platformlayer.auth.ProjectEntity;
import org.platformlayer.auth.ServiceAccountEntity;
import org.platformlayer.auth.UserDatabase;
import org.platformlayer.auth.UserEntity;
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

		bind(UserDatabase.class).to(JdbcUserRepository.class).asEagerSingleton();
		bind(KeystoneUserAuthenticator.class).to(KeystoneOpsAuthenticator.class).asEagerSingleton();

		bind(SystemAuthenticator.class).to(ClientCertificateSystemAuthenticator.class).asEagerSingleton();

		bind(ResultSetMappers.class).toProvider(
				ResultSetMappersProvider.build(UserEntity.class, ProjectEntity.class, ServiceAccountEntity.class));
	}
}
