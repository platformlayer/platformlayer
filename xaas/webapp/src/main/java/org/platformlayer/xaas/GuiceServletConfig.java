package org.platformlayer.xaas;

import java.util.List;

import org.platformlayer.guice.JdbcGuiceModule;
import org.platformlayer.ops.GuiceOpsConfig;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceServletContextListener;

public class GuiceServletConfig extends GuiceServletContextListener {
	@Override
	protected Injector getInjector() {
		List<Module> modules = Lists.newArrayList();
		addModules(modules);

		return Guice.createInjector(modules);
	}

	protected void addModules(List<Module> modules) {
		modules.add(new GuiceOpsConfig());
		modules.add(new GuiceXaasConfig());
		modules.add(new JdbcGuiceModule());
		addServletModule(modules);
	}

	protected void addServletModule(List<Module> modules) {
		modules.add(new PlatformLayerServletModule());
	}
}
