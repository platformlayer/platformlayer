package org.platformlayer.config;

import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.inject.MembersInjector;

class ConfiguredFieldInjector<T> implements MembersInjector<T> {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ConfiguredFieldInjector.class);

	final Configuration configuration;
	final Field field;

	final Configured annotation;

	final Class<?> fieldType;

	final String configurationKey;

	final FieldBinding fieldBinding;

	public ConfiguredFieldInjector(Configuration configuration, Field field) {
		this.configuration = configuration;
		this.field = field;

		this.annotation = field.getAnnotation(Configured.class);
		this.fieldType = field.getType();

		this.configurationKey = buildConfigurationKey();

		this.fieldBinding = buildFieldBinding();
	}

	private String buildConfigurationKey() {
		String key = annotation.value();
		if (Strings.isNullOrEmpty(key)) {
			key = field.getDeclaringClass().getSimpleName() + "." + field.getName();
		}
		return key;
	}

	private FieldBinding buildFieldBinding() {
		return FieldBinding.build(field);
	}

	@Override
	public void injectMembers(T instance) {
		String value = configuration.find(configurationKey);
		if (value == null) {
			// TODO: Support required attribute ?
			return;
		}

		fieldBinding.setValue(instance, value);
	}

}
