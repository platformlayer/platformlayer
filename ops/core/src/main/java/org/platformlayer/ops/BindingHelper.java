package org.platformlayer.ops;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.platformlayer.jdbc.simplejpa.ReflectionUtils;

import com.google.common.collect.Lists;
import com.google.inject.Injector;

public class BindingHelper {
	public static final BindingHelper INSTANCE = new BindingHelper(null, null);

	final Injector injector;
	final Map<Class<?>, Object> prebound;

	public BindingHelper(Injector injector, Map<Class<?>, Object> prebound) {
		this.injector = injector;
		this.prebound = prebound;
	}

	public void bind(Object item) {
		Class<? extends Object> itemClass = item.getClass();
		for (Field field : ReflectionUtils.getAllFields(itemClass)) {
			Bound boundAnnotation = field.getAnnotation(Bound.class);

			if (boundAnnotation != null) {
				Object fieldValue = getFieldValue(field);

				field.setAccessible(true);
				try {
					field.set(item, fieldValue);
				} catch (IllegalAccessException e) {
					throw new IllegalStateException("Error setting field: " + field, e);
				}
			}
		}
	}

	private Object getFieldValue(Field field) {
		Class<?> fieldType = field.getType();
		Object item = null;
		if (prebound != null) {
			item = prebound.get(fieldType);

			if (item == null) {
				List<Object> matches = Lists.newArrayList();

				for (Entry<Class<?>, Object> entry : prebound.entrySet()) {
					Class<?> entryClass = entry.getKey();
					if (fieldType.isAssignableFrom(entryClass)) {
						matches.add(entry.getValue());
					}
				}

				if (!matches.isEmpty()) {
					if (matches.size() > 1) {
						throw new IllegalStateException("Found multiple potential bindings for class: " + fieldType);
					}
					item = matches.get(0);
				}
			}
		}

		if (item == null) {
			OpsContext opsContext = OpsContext.get();
			if (opsContext != null) {
				item = opsContext.getInstance(fieldType);
			}
		}

		if (item == null) {
			item = getInjector().getInstance(fieldType);
		}

		if (item == null) {
			throw new IllegalArgumentException("Cannot find bound value for: " + field);
		}

		BindingHelper.INSTANCE.bind(item);

		return item;
	}

	private Injector getInjector() {
		if (injector != null) {
			return injector;
		}
		return OpsContext.get().getInjector();
	}

}
