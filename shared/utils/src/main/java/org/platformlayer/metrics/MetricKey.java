package org.platformlayer.metrics;

import java.lang.reflect.Method;

public class MetricKey {
	final String group;
	final String className;
	final String name;

	public MetricKey(String group, String className, String name) {
		super();
		this.group = group;
		this.className = className;
		this.name = name;
	}

	private static String buildPackageName(Class<?> clazz) {
		return clazz.getPackage() == null ? "" : clazz.getPackage().getName();
	}

	private static String buildClassName(Class<?> clazz) {
		String name = clazz.getSimpleName();
		name = name.replaceAll("\\$$", "");
		return name;
	}

	public static MetricKey forMethod(Class<?> clazz, Method method) {
		return build(clazz, method.getName());
	}

	public static MetricKey build(Class<?> clazz, String name) {
		return new MetricKey(buildPackageName(clazz), buildClassName(clazz), name);
	}

	public String getTypeName() {
		return className;
	}

	public String getGroup() {
		return group;
	}

	public String getName() {
		return name;
	}

}
