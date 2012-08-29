package org.platformlayer.metrics;

import java.lang.reflect.Method;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.ext.Provider;

import org.platformlayer.config.Configured;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.spi.container.ResourceMethodDispatchAdapter;
import com.sun.jersey.spi.container.ResourceMethodDispatchProvider;
import com.sun.jersey.spi.dispatch.RequestDispatcher;

@Provider
@Singleton
public class JerseyMetricsHook implements ResourceMethodDispatchAdapter {
	@Inject
	MetricsSystem metrics;

	@Configured
	boolean instrumentAllMethods = true;

	@Override
	public ResourceMethodDispatchProvider adapt(ResourceMethodDispatchProvider provider) {
		return new InstrumentedResourceMethodDispatchProvider(provider);
	}

	static class InstrumentedRequestDispatcher implements RequestDispatcher {
		private final RequestDispatcher underlying;
		private final MetricTimer timer;

		private InstrumentedRequestDispatcher(RequestDispatcher underlying, MetricTimer timer) {
			this.underlying = underlying;
			this.timer = timer;
		}

		@Override
		public void dispatch(Object resource, HttpContext httpContext) {
			final MetricTimer.Context context = timer.start();
			try {
				underlying.dispatch(resource, httpContext);
			} finally {
				context.stop();
			}
		}
	}

	class InstrumentedResourceMethodDispatchProvider implements ResourceMethodDispatchProvider {
		private final ResourceMethodDispatchProvider provider;

		public InstrumentedResourceMethodDispatchProvider(ResourceMethodDispatchProvider provider) {
			this.provider = provider;
		}

		@Override
		public RequestDispatcher create(AbstractResourceMethod jerseyMethod) {
			RequestDispatcher dispatcher = provider.create(jerseyMethod);
			if (dispatcher == null) {
				return null;
			}

			// TODO: Should we cache the timer per-method?
			Method method = jerseyMethod.getMethod();

			boolean instrument = false;
			if (instrumentAllMethods) {
				instrument = true;
			} else {
				Instrumented annotation = findAnnotation(method);
				if (annotation != null) {
					instrument = true;
				}
			}

			if (instrument) {
				Class<?> clazz = jerseyMethod.getDeclaringResource().getResourceClass();
				MetricKey metricKey = MetricKey.build(clazz, method);

				MetricTimer timer = metrics.getTimer(metricKey);
				if (timer != null) {
					dispatcher = new InstrumentedRequestDispatcher(dispatcher, timer);
				}
			}

			return dispatcher;
		}
	}

	public Instrumented findAnnotation(Method method) {
		Instrumented annotation = method.getAnnotation(Instrumented.class);
		if (annotation == null) {
			annotation = method.getDeclaringClass().getAnnotation(Instrumented.class);
		}
		return annotation;
	}
}
