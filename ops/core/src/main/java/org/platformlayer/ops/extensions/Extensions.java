package org.platformlayer.ops.extensions;

import java.util.List;

import org.platformlayer.jdbc.simplejpa.ResultSetMappersProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.Configuration;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

public class Extensions {
	private static final Logger log = LoggerFactory.getLogger(Extensions.class);

	private final Configuration configuration;

	final List<ExtensionModule> extensions = Lists.newArrayList();

	private Extensions(Configuration configuration) {
		this.configuration = configuration;

		loadExtensions();
	}

	public static Extensions load(Configuration configuration) {
		return new Extensions(configuration);
	}

	public void getInjector() {

	}

	private void loadExtensions() {
		String extensionList = configuration.find("extensions");
		if (!Strings.isNullOrEmpty(extensionList)) {
			for (String extension : Splitter.on(',').split(extensionList)) {
				log.info("Using extension: " + extension);

				Class<? extends ExtensionModule> extensionClass;
				try {
					if (!extension.contains(".")) {
						extension = "org.platformlayer.ops.extensions." + extension;
					}
					extensionClass = (Class<? extends ExtensionModule>) Class.forName(extension);
				} catch (ClassNotFoundException e) {
					throw new IllegalStateException("Unable to load extension class: " + extension, e);
				}
				ExtensionModule extensionModule;
				try {
					extensionModule = extensionClass.newInstance();
				} catch (InstantiationException e) {
					throw new IllegalStateException("Unable to construct extension class: " + extension, e);
				} catch (IllegalAccessException e) {
					throw new IllegalStateException("Unable to construct extension class: " + extension, e);
				}

				extensions.add(extensionModule);
			}
		}

	}

	public void addFilters(HttpConfiguration http) {
		for (ExtensionModule extension : extensions) {
			extension.addFilters(http);
		}
	}

	public void addEntities(ResultSetMappersProvider resultSetMappersProvider) {
		for (ExtensionModule extension : extensions) {
			extension.addEntities(resultSetMappersProvider);
		}
	}

	public Injector createInjector(List<Module> modules) {
		List<Module> overrides = Lists.newArrayList();

		for (ExtensionModule extension : extensions) {
			Module module = extension.getOverrideModule();
			if (module != null) {
				overrides.add(module);
			}
		}

		if (overrides.isEmpty()) {
			return Guice.createInjector(modules);
		} else {
			Module combined = Modules.override(modules).with(overrides);
			return Guice.createInjector(combined);
		}
	}

}
