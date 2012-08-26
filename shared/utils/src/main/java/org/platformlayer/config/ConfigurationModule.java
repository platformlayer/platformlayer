package org.platformlayer.config;

import java.util.Map.Entry;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

public class ConfigurationModule extends AbstractModule {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(ConfigurationModule.class);

	private final Configuration configuration;

	public ConfigurationModule(Configuration configuration) {
		this.configuration = configuration;
	}

	public ConfigurationModule() {
		this(Configuration.load());
	}

	@Override
	protected void configure() {
		bind(Configuration.class).toInstance(configuration);

		// TODO: Do we really need this??
		configuration.bindProperties(binder());

		bindListener(Matchers.any(), new ConfigurationTypeListener(configuration));

		try {
			doExplicitBindings(configuration);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException("Explicitly bound class not found during binding", e);
		}
	}

	private void doExplicitBindings(Configuration configuration) throws ClassNotFoundException {
		Properties bindings = configuration.getChildProperties("bind.");
		for (Entry<Object, Object> entry : bindings.entrySet()) {
			String serviceKey = (String) entry.getKey();
			Class service = Class.forName(serviceKey);
			Class implementation = Class.forName((String) entry.getValue());

			bind(service).to(implementation);
		}
	}
}
