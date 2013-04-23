package org.platformlayer.extensions;

import java.util.List;
import java.util.ServiceLoader;

import javax.inject.Inject;

import org.platformlayer.jdbc.simplejpa.ResultSetMappersProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.Configuration;
import com.fathomdb.discovery.Discovery;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

public class Extensions {
	private static final Logger log = LoggerFactory.getLogger(Extensions.class);

	final List<ExtensionModule> extensions = Lists.newArrayList();

	@Inject
	public Extensions(Configuration configuration, Discovery reflection) {
		if (configuration != null) {
			loadExtensions(configuration);
		}

		if (reflection != null) {
			loadReflectedExtensions(reflection);
		}
	}

	// public static Extensions load(Configuration configuration, ReflectionDiscovery reflection) {
	// return new Extensions(configuration, reflection);
	// }

	public void getInjector() {

	}

	private void loadExtensions(Configuration configuration) {
		String extensionList = configuration.find("extensions");
		if (!Strings.isNullOrEmpty(extensionList)) {
			for (String extension : Splitter.on(',').split(extensionList)) {
				log.info("Using configured extension: " + extension);

				Class<? extends ExtensionModule> extensionClass;
				try {
					if (!extension.contains(".")) {
						extension = "org.platformlayer.extensions." + extension;
					}
					ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
					extensionClass = (Class<? extends ExtensionModule>) classLoader.loadClass(extension);
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

	private void loadReflectedExtensions(Discovery discovery) {
		ServiceLoader<ExtensionModule> serviceLoader = ServiceLoader.load(ExtensionModule.class);

		int count = 0;

		for (ExtensionModule extension : serviceLoader) {
			extensions.add(extension);
			count++;
		}

		if (count == 0) {
			log.info("No Annotated extensions found");
		}

		// Collection<Class> extensionTypes = discovery.findAnnotatedClasses(Extension.class);
		// if (extensionTypes == null || extensionTypes.isEmpty()) {
		// log.info("No Annotated extensions found");
		// return;
		// }
		//
		// List<ExtensionModule> instances = Discovery.buildInstances(ExtensionModule.class, extensionTypes);
		// for (ExtensionModule instance : instances) {
		// extensions.add(instance);
		// }
	}

	public void addHttpExtensions(HttpConfiguration http) {
		for (ExtensionModule extension : extensions) {
			extension.addHttpExtensions(http);
		}
	}

	public void addEntities(ResultSetMappersProvider resultSetMappersProvider) {
		for (ExtensionModule extension : extensions) {
			extension.addEntities(resultSetMappersProvider);
		}
	}

	public Injector createInjector(Configuration configuration, List<Module> modules) {
		for (ExtensionModule extension : extensions) {
			List<Module> extraModules = extension.getExtraModules(configuration);
			if (extraModules != null) {
				modules.addAll(extraModules);
			}
		}

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
