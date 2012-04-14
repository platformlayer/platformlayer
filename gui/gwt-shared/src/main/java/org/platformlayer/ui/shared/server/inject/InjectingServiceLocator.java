package org.platformlayer.ui.shared.server.inject;

import javax.inject.Inject;

import com.google.inject.Injector;
import com.google.web.bindery.requestfactory.shared.ServiceLocator;

public class InjectingServiceLocator implements ServiceLocator {

	@Inject
	Injector injector;

	@Override
	public Object getInstance(Class<?> clazz) {
		return injector.getInstance(clazz);
	}

}
