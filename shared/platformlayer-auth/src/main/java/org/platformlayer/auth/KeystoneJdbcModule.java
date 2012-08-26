package org.platformlayer.auth;

import javax.sql.DataSource;

import org.platformlayer.guice.GuiceDataSourceProvider;
import org.platformlayer.guice.JdbcGuiceModule;
import org.platformlayer.jdbc.simplejpa.ResultSetMappers;
import org.platformlayer.jdbc.simplejpa.ResultSetMappersProvider;

import com.google.inject.AbstractModule;

public class KeystoneJdbcModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(DataSource.class).toProvider(GuiceDataSourceProvider.bind("auth.jdbc."));

		JdbcGuiceModule jdbcGuiceModule = new JdbcGuiceModule();
		binder().install(jdbcGuiceModule);

		bind(UserDatabase.class).to(JdbcUserRepository.class).asEagerSingleton();
		bind(ResultSetMappers.class).toProvider(
				ResultSetMappersProvider.build(UserEntity.class, ProjectEntity.class, ServiceAccountEntity.class,
						UserProjectEntity.class, UserCertEntity.class));
	}
}
