//package org.platformlayer.common;
//
//
//public class EnumTagDefinition<E extends Enum<E>> extends TagDefinition {
//
//	private final Class<E> enumClass;
//
//	protected EnumTagDefinition(String key, Class<E> enumClass) {
//		super(key);
//		this.enumClass = enumClass;
//	}
//
//	public E get(HasTags tags) {
//		String s = tags.findFirst(key);
//		if (s == null) {
//			return null;
//		}
//
//		for (E e : enumClass.getEnumConstants()) {
//			String v = e.name();
//			if (s.equalsIgnoreCase(v)) {
//				return e;
//			}
//		}
//
//		throw new IllegalArgumentException("Unknown status: " + s);
//	}
//
//	public static <E extends Enum<E>> EnumTagDefinition<E> build(String key, Class<E> enumClass) {
//		return new EnumTagDefinition<E>(key, enumClass);
//	}
// }
