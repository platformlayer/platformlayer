package org.platformlayer.xaas;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

public class GuiceServletConfig extends GuiceServletContextListener {
	final Injector injector;

	public GuiceServletConfig(Injector injector) {
		super();
		this.injector = injector;
	}

	@Override
	protected Injector getInjector() {
		return injector;
	}
}
