package org.platformlayer.config;

import java.lang.reflect.Field;

import com.fathomdb.Configuration;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

public class ConfigurationTypeListener implements TypeListener {

	final Configuration configuration;

	public ConfigurationTypeListener(Configuration configuration) {
		super();
		this.configuration = configuration;
	}

	@Override
	public <T> void hear(TypeLiteral<T> typeLiteral, TypeEncounter<T> encounter) {
		Class<? super T> clazz = typeLiteral.getRawType();

		do {
			for (Field field : clazz.getDeclaredFields()) {
				Configured annotation = field.getAnnotation(Configured.class);
				if (annotation != null) {
					encounter.register(new ConfiguredFieldInjector<T>(configuration, field));
				}
			}
		} while ((clazz = clazz.getSuperclass()) != null);
	}
}