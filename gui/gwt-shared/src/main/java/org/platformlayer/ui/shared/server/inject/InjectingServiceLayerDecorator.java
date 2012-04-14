package org.platformlayer.ui.shared.server.inject;

import javax.inject.Inject;

import com.google.inject.Injector;
import com.google.web.bindery.requestfactory.server.ServiceLayerDecorator;
import com.google.web.bindery.requestfactory.shared.Locator;
import com.google.web.bindery.requestfactory.shared.ServiceLocator;

public class InjectingServiceLayerDecorator extends ServiceLayerDecorator {

	@Inject
	Injector injector;

	@Override
	public <T extends Locator<?, ?>> T createLocator(Class<T> clazz) {
		return injector.getInstance(clazz);
	}

	@Override
	public <T extends ServiceLocator> T createServiceLocator(Class<T> clazz) {
		return injector.getInstance(clazz);
	}

}
