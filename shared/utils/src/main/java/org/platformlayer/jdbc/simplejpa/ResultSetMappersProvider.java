package org.platformlayer.jdbc.simplejpa;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.inject.Provider;

public class ResultSetMappersProvider implements Provider<ResultSetMappers> {

	private ResultSetMappers resultSetMappers;

	private final List<Class> modelClasses = Lists.newArrayList();

	@Override
	public ResultSetMappers get() {
		if (resultSetMappers == null) {
			seal();
		}
		return resultSetMappers;
	}

	public synchronized void seal() {
		if (resultSetMappers != null) {
			throw new IllegalStateException();
		}
		Class[] modelClassesArray = modelClasses.toArray(new Class[modelClasses.size()]);
		resultSetMappers = new ResultSetMappers(DatabaseNameMapping.POSTGRESQL, modelClassesArray);
	}

	public void add(Class clazz) {
		if (resultSetMappers != null) {
			throw new IllegalStateException("Result set mapping has already been built");
		}
		modelClasses.add(clazz);
	}

	public static ResultSetMappersProvider build(Class... classes) {
		ResultSetMappersProvider provider = new ResultSetMappersProvider();
		for (Class clazz : classes) {
			provider.add(clazz);
		}

		// provider.seal();

		return provider;
	}

	public void addAll(Class<?>... classes) {
		for (Class<?> clazz : classes) {
			add(clazz);
		}
	}
}
