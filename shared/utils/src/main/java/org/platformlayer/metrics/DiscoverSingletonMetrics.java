package org.platformlayer.metrics;

import java.lang.annotation.Annotation;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Scope;
import com.google.inject.spi.BindingScopingVisitor;

public class DiscoverSingletonMetrics {
	private static final Logger log = LoggerFactory.getLogger(DiscoverSingletonMetrics.class);

	@Inject
	MetricRegistry metricsRegistry;

	@Inject
	Injector injector;

	public void discover() {
		for (final Binding<?> binding : injector.getAllBindings().values()) {
			binding.acceptScopingVisitor(new BindingScopingVisitor<Void>() {

				@Override
				public Void visitEagerSingleton() {
					log.debug("Found eager singleton: " + binding);
					Object o = binding.getProvider().get();
					metricsRegistry.discoverMetrics(o);
					return null;
				}

				@Override
				public Void visitScope(Scope scope) {
					log.debug("Ignoring binding in scope: " + scope);

					return null;
				}

				@Override
				public Void visitScopeAnnotation(Class<? extends Annotation> scopeAnnotation) {
					log.debug("Ignoring binding in scope: " + scopeAnnotation);
					return null;
				}

				@Override
				public Void visitNoScoping() {
					log.debug("Ignoring binding in NoScope: " + binding);
					return null;
				}
			});
		}

	}
}
