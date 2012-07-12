package org.openstack.keystone.server;

import javax.inject.Inject;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

public class GuiceServletConfig extends GuiceServletContextListener {
	@Inject
	Injector injector;

	@Override
	protected Injector getInjector() {
		return injector;
	}
}
