package org.platformlayer.metrics;

import java.lang.reflect.Method;

public class MetricKey {
	final Class<?> sourceClass;

	final String[] path;

	public MetricKey(Class<?> sourceClass, String[] path) {
		super();
		this.sourceClass = sourceClass;
		this.path = path;
	}

	public static MetricKey build(Class<?> sourceClass, Method method) {
		return build(sourceClass, method.getName());
	}

	public static MetricKey build(Class<?> sourceClass, String name) {
		return new MetricKey(sourceClass, new String[] { name });
	}

	public Class<?> getSourceClass() {
		return sourceClass;
	}

	public String[] getPath() {
		return path;
	}

}
