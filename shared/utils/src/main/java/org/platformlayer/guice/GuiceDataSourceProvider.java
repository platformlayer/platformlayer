package org.platformlayer.guice;

import java.lang.annotation.Annotation;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;

public class GuiceDataSourceProvider implements Provider<DataSource> {
	// private final String url;
	// private final String username;
	// private final String password;
	// private final String driverClass;
	private final String prefix;

	@Inject
	Injector injector;

	private final Properties properties;

	// @Inject
	// public GuiceDataSourceProvider(@Named("driverClassName") final String driverClass, @Named("url") final String
	// url, @Named("username") final String username,
	// @Named("password") final String password) {
	// this.prefix = null;
	//
	// this.driverClass = driverClass;
	// this.url = url;
	// this.username = username;
	// this.password = password;
	// }

	public GuiceDataSourceProvider(String prefix, Properties properties) {
		this.properties = properties;
		this.prefix = prefix;
	}

	@Override
	public DataSource get() {
		BasicDataSource pooledDataSource = new BasicDataSource();

		String keyPrefix = this.prefix;
		if (keyPrefix == null) {
			keyPrefix = "";
		}

		pooledDataSource.setDriverClassName(getProperty(keyPrefix + "driverClassName"));
		pooledDataSource.setUrl(getProperty(keyPrefix + "url"));
		pooledDataSource.setUsername(getProperty(keyPrefix + "username"));
		pooledDataSource.setPassword(getProperty(keyPrefix + "password"));

		return pooledDataSource;
	}

	private String getProperty(final String key) {
		if (properties != null) {
			return (String) properties.get(key);
		}

		Named annotation = new Named() {
			@Override
			public Class<? extends Annotation> annotationType() {
				return Named.class;
			}

			@Override
			public String value() {
				return key;
			}
		};

		Key<String> bindingKey = Key.get(String.class, annotation);

		Binding<String> binding = injector.getBinding(bindingKey);
		return binding.getProvider().get();
	}
}