package org.platformlayer.common;

import org.platformlayer.core.model.Tag;

public class EnumTagKey<E extends Enum<E>> extends TagKey<E> {
	private final Class<E> enumClass;

	public EnumTagKey(String key, Class<E> enumClass) {
		super(key, null);
		this.enumClass = enumClass;
	}

	@Override
	protected E toT(String s) {
		if (s == null) {
			return null;
		}

		for (E e : enumClass.getEnumConstants()) {
			String v = e.name();
			if (s.equalsIgnoreCase(v)) {
				return e;
			}
		}

		throw new IllegalArgumentException("Unknown status: " + s);
	}

	public Tag build(E t) {
		return new Tag(key, t.name());
	}

	public static <E extends Enum<E>> EnumTagKey<E> build(String key, Class<E> enumClass) {
		return new EnumTagKey<E>(key, enumClass);
	}
}