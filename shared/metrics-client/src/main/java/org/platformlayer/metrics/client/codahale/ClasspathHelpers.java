package org.platformlayer.metrics.client.codahale;

public class ClasspathHelpers {
	public static boolean isOnClasspath(String className) {
		try {
			Class.forName(className);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	public static boolean isJerseyOnClasspath() {
		return isOnClasspath("com.sun.jersey.spi.container.ResourceMethodDispatchAdapter");
	}
}
