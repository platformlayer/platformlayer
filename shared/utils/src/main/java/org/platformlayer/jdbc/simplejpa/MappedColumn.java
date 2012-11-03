package org.platformlayer.jdbc.simplejpa;

import java.lang.reflect.Field;

import org.platformlayer.shared.EnumWithKey;
import org.platformlayer.shared.EnumWithKeys;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

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

		if (char.class.isAssignableFrom(fieldType)) {
			if (value instanceof String) {
				String stringValue = (String) value;
				if (Strings.isNullOrEmpty(stringValue)) {
					value = 0;
				} else if (stringValue.length() == 1) {
					value = stringValue.charAt(0);
				} else {
					throw new IllegalArgumentException("Cannot map multi-character string to char type: " + field);
				}
			}
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
