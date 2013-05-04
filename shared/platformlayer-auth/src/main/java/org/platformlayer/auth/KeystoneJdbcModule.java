package org.platformlayer.auth;

import javax.sql.DataSource;

import org.platformlayer.jdbc.GuiceDataSourceProvider;

import com.fathomdb.jdbc.JdbcGuiceModule;
import com.fathomdb.jpa.impl.ResultSetMappers;
import com.fathomdb.jpa.impl.ResultSetMappersProvider;
import com.google.inject.AbstractModule;
import com.google.inject.util.Providers;

public class KeystoneJdbcModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(DataSource.class).toProvider(GuiceDataSourceProvider.bind("auth.jdbc.")).asEagerSingleton();

		JdbcGuiceModule jdbcGuiceModule = new JdbcGuiceModule();
		binder().install(jdbcGuiceModule);

		bind(UserDatabase.class).to(JdbcUserRepository.class).asEagerSingleton();
		bind(ResultSetMappers.class).toProvider(
				Providers.guicify(ResultSetMappersProvider.build(UserEntity.class, ProjectEntity.class,
						ServiceAccountEntity.class, UserProjectEntity.class, UserCertEntity.class)));
	}
}
