package org.platformlayer.ops.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Injector;

public class MethodInvoker {
	@Inject
	Injector injector;

	final Map<Class<?>, Object> parameters = Maps.newHashMap();
	final List<Function<Class<?>, Object>> providers = Lists.newArrayList();

	public MethodInvoker() {
	}

	public <T, U extends T> void bind(Class<T> clazz, U object) {
		parameters.put(clazz, object);
	}

	public void addProvider(Function<Class<?>, Object> provider) {
		providers.add(provider);
	}

	public void invokeMethod(Object target, Method method) throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		Object[] parameters = fillParameters(method);

		method.invoke(target, parameters);
	}

	private Object[] fillParameters(Method method) {
		Class<?>[] parameterTypes = method.getParameterTypes();
		Annotation[][] parameterAnnotations = method.getParameterAnnotations();

		Object[] parameters = new Object[parameterTypes.length];
		for (int i = 0; i < parameters.length; i++) {
			try {
				parameters[i] = fillParameter(method, parameterTypes[i], parameterAnnotations[i]);
			} catch (Exception e) {
				throw new IllegalArgumentException("Error injecting parameter #" + (i + 1) + " of type "
						+ parameterTypes[i] + " of method " + method, e);
			}
		}
		return parameters;
	}

	protected Object fillParameter(Method method, Class<?> parameterClass, Annotation[] parameterAnnotations) {
		Object instance = null;

		if (instance == null) {
			instance = parameters.get(parameterClass);
		}

		if (instance == null) {
			for (Function<Class<?>, Object> provider : providers) {
				instance = provider.apply(parameterClass);
				if (instance != null) {
					break;
				}
			}
		}

		if (instance == null) {
			instance = injector.getInstance(parameterClass);
		}

		if (instance == null) {
			throw new IllegalArgumentException("Cannot bind argument: " + parameterClass + " of " + method);
		}

		return instance;
	}
}
