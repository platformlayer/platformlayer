package org.platformlayer.metrics;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;

import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

public class InstrumentedListener implements TypeListener {
	final MetricsSystem metricsSystem;

	public InstrumentedListener(MetricsSystem metricsSystem) {
		this.metricsSystem = metricsSystem;
	}

	@Override
	public <T> void hear(TypeLiteral<T> literal, TypeEncounter<T> encounter) {
		Class<? super T> klass = literal.getRawType();

		do {
			for (Method method : klass.getDeclaredMethods()) {
				final MethodInterceptor interceptor = InstrumentedInterceptor.forMethod(metricsSystem, klass, method);
				if (interceptor != null) {
					encounter.bindInterceptor(Matchers.only(method), interceptor);
				}
			}
		} while ((klass = klass.getSuperclass()) != null);
	}
}