package org.platformlayer.ops;

import java.lang.reflect.Field;

import org.platformlayer.jdbc.simplejpa.ReflectionUtils;

public class BindingHelper {
	public static final BindingHelper INSTANCE = new BindingHelper();

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
		Object item = OpsContext.get().getInstance(fieldType);
		if (item == null) {
			item = OpsContext.get().getInjector().getInstance(fieldType);

			if (item == null) {
				throw new IllegalArgumentException("Cannot find bound value for: " + field);
			}
		}

		BindingHelper.INSTANCE.bind(item);

		return item;
	}

}
