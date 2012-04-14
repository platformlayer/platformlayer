package org.platformlayer.ops;

import java.util.Properties;

import javax.inject.Provider;

public abstract class OpsProvider<T> {

	public abstract T get() throws OpsException;

	// @Override
	// public T Provider<T>.get() {
	// try {
	// return get();
	// }
	// catch (OpsException e) {
	// throw new IllegalStateException("Error computing value", e);
	// }
	// }

	public static OpsProvider<String> getProperty(final Provider<Properties> propertiesProvider, final String key) {
		return new OpsProvider<String>() {
			@Override
			public String get() {
				Properties properties = propertiesProvider.get();
				return properties.getProperty(key);
			}
		};
	}

	public static <T> OpsProvider<T> of(final T item) {
		return new OpsProvider<T>() {
			@Override
			public T get() {
				return item;
			}
		};
	}
}
