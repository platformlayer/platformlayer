package org.platformlayer.config;

import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FieldBinding {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(FieldBinding.class);

	final Field field;

	protected FieldBinding(Field field) {
		this.field = field;
	}

	public static FieldBinding build(Field field) {
		Class<?> fieldType = field.getType();

		if (fieldType == Integer.class || fieldType == int.class) {
			return new IntegerFieldBinding(field);
		}

		if (fieldType == Boolean.class || fieldType == boolean.class) {
			return new BooleanFieldBinding(field);
		}

		throw new IllegalStateException("Unhandled field type: " + fieldType.getSimpleName());
	}

	static class IntegerFieldBinding extends FieldBinding {

		public IntegerFieldBinding(Field field) {
			super(field);
		}

		@Override
		public void setValue(Object target, String s) {
			int value = Integer.valueOf(s);
			setValue(target, value);
		}

		public void setValue(Object target, Integer v) {
			try {
				field.set(target, v);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Error setting field", e);
			}
		}

	}

	static class BooleanFieldBinding extends FieldBinding {

		public BooleanFieldBinding(Field field) {
			super(field);
		}

		@Override
		public void setValue(Object target, String s) {
			Boolean value = Boolean.valueOf(s);
			setValue(target, value);
		}

		public void setValue(Object target, Boolean v) {
			try {
				field.set(target, v);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Error setting field", e);
			}
		}

	}

	public abstract void setValue(Object target, String value);

}
