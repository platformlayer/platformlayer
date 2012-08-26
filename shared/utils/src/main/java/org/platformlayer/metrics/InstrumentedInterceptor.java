package org.platformlayer.metrics;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * A method interceptor which creates a timer for the declaring class with the given name (or the method's name, if none
 * was provided), and which times the execution of the annotated method.
 */
class InstrumentedInterceptor implements MethodInterceptor {
	static MethodInterceptor forMethod(MetricsSystem metricsSystem, Class<?> clazz, Method method) {
		Instrumented annotation = method.getAnnotation(Instrumented.class);
		if (annotation == null) {
			annotation = clazz.getAnnotation(Instrumented.class);
		}

		if (annotation != null) {
			MetricKey metricKey = MetricKey.forMethod(clazz, method);
			MetricTimer timer = metricsSystem.buildNewTimer(metricKey);
			if (timer != null) {
				return new InstrumentedInterceptor(timer);
			}
		}

		return null;
	}

	private final MetricTimer timer;

	private InstrumentedInterceptor(MetricTimer timer) {
		this.timer = timer;
	}

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		final MetricTimer.Context ctx = timer.start();
		try {
			return invocation.proceed();
		} finally {
			ctx.stop();
		}
	}
}