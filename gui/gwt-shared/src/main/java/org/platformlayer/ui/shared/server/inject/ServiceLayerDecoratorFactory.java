package org.platformlayer.ui.shared.server.inject;

import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.google.inject.Injector;
import com.google.web.bindery.requestfactory.server.ServiceLayerDecorator;

public class ServiceLayerDecoratorFactory {

	List<ServiceLayerDecorator> decorators = Lists.newArrayList();

	public ServiceLayerDecorator[] buildServiceLayerDecorators() {
		addDecorators();

		return decorators.toArray(new ServiceLayerDecorator[decorators.size()]);
	}

	@Inject
	Injector injector;

	protected void addDecorators() {
		addDecorator(InjectingServiceLayerDecorator.class);
	}

	protected <T extends ServiceLayerDecorator> T addDecorator(Class<T> clazz) {
		T instance = injector.getInstance(clazz);
		decorators.add(instance);
		return instance;
	}
}
