package org.platformlayer.ops.packages;

import java.util.Map;

import javax.inject.Inject;

import org.slf4j.*;
import org.platformlayer.ops.OpsContext;

import com.google.inject.Injector;
import com.google.inject.Provider;

public class OpsContextProvider<T> implements Provider<T> {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(OpsContextProvider.class);

	final Class<T> clazz;

	public OpsContextProvider(Class<T> clazz) {
		this.clazz = clazz;
	}

	@Inject
	Injector injector;

	@Override
	public T get() {
		OpsContext opsContext = OpsContext.get();
		if (opsContext == null) {
			throw new IllegalStateException();
		}

		Map<Object, Object> cacheMap = opsContext.getCacheMap();
		T item = (T) cacheMap.get(clazz);
		if (item == null) {
			item = injector.getInstance(clazz);
			cacheMap.put(clazz, item);
		}
		return item;
	}
}
