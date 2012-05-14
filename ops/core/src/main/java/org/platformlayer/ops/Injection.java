package org.platformlayer.ops;

public class Injection {
	public static <T> T getInstance(Class<T> clazz) {
		return OpsContext.get().getInjector().getInstance(clazz);
	}

	public static void injectMembers(Object instance) {
		OpsContext.get().getInjector().injectMembers(instance);
	}
}
