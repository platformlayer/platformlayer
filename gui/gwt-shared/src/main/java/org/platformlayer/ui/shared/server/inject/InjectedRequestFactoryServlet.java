package org.platformlayer.ui.shared.server.inject;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.web.bindery.requestfactory.server.ExceptionHandler;
import com.google.web.bindery.requestfactory.server.RequestFactoryServlet;

@Singleton
public class InjectedRequestFactoryServlet extends RequestFactoryServlet {
	private static final long serialVersionUID = 1L;

	@Inject
	protected InjectedRequestFactoryServlet(ExceptionHandler exceptionHandler,
			ServiceLayerDecoratorFactory decoratorsFactory) {
		super(exceptionHandler, decoratorsFactory.buildServiceLayerDecorators());
	}
}
