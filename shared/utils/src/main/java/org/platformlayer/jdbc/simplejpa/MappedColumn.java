package org.platformlayer.jdbc.simplejpa;

import java.lang.reflect.Field;

import org.platformlayer.jdbc.EnumWithKey;
import org.platformlayer.jdbc.EnumWithKeys;

import com.google.common.base.Objects;

public class MappedColumn {
	final Field field;
	final Class<?> fieldType;

	public MappedColumn(Field field) {
		super();
		this.field = field;
		this.fieldType = field.getType();
	}

	public void setField(Object target, Object value) {
		// TODO: Precompute whether we need a transform?

		if (EnumWithKey.class.isAssignableFrom(fieldType)) {
			value = EnumWithKeys.fromKey((Class<? extends EnumWithKey>) fieldType, (String) value);
		}

		try {
			field.set(target, value);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Error setting field: " + field.getName(), e);
		}
	}

	public boolean isField(Field field) {
		return Objects.equal(field, this.field);
	}
}
