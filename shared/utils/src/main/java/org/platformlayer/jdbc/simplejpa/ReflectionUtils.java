package org.platformlayer.jdbc.simplejpa;

import java.lang.reflect.Field;
import java.util.List;

import com.google.common.collect.Lists;

public class ReflectionUtils {

	public static Field findField(Class<?> clazz, String fieldName) {
		for (Field field : clazz.getDeclaredFields()) {
			if (field.getName().equals(fieldName)) {
				return field;
			}
		}
		return null;
	}

	public static List<Field> getAllFields(Class<?> clazz) {
		List<Field> fields = Lists.newArrayList();

		Class<?> c = clazz;
		while (true) {
			if (c == null || c == Object.class) {
				break;
			}

			for (Field field : c.getDeclaredFields()) {
				fields.add(field);
			}

			c = c.getSuperclass();
		}
		return fields;
	}

}
