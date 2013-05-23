package org.platformlayer.config;

import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.Configuration;
import com.fathomdb.config.ConfigurationImpl;
import com.fathomdb.config.SimpleConfigurationModule;
import com.google.inject.matcher.Matchers;

public class ConfigurationModule extends SimpleConfigurationModule {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ConfigurationModule.class);

	public ConfigurationModule(ConfigurationImpl configuration) {
		super(configuration);
	}

	public ConfigurationModule() {
		super();
	}

	@Override
	protected void configure() {
		super.configure();

		ConfigurationImpl configuration = getConfiguration();

		// TODO: Do we really need this??
		// configuration.bindProperties(binder());

		bindListener(Matchers.any(), new ConfigurationTypeListener(configuration));

		try {
			doExplicitBindings(configuration);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException("Explicitly bound class not found during binding", e);
		}
	}

	private void doExplicitBindings(Configuration configuration) throws ClassNotFoundException {
		Map<String, String> bindings = configuration.getChildProperties("bind.");
		for (Entry<String, String> entry : bindings.entrySet()) {
			String serviceKey = entry.getKey();

			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

			try {
				Class service = classLoader.loadClass(serviceKey);
				Class implementation = classLoader.loadClass(entry.getValue());
				bind(service).to(implementation);
			} catch (ClassNotFoundException e) {
				log.warn("Unable to load class", e);
				throw e;
			}
		}
	}
}
