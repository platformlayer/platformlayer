package org.platformlayer.shared;

public class EnumWithKeys {

	public static <T extends EnumWithKey> T fromKey(Class<T> enumType, String key) {
		// TODO: Put into map?
		for (T enumValue : enumType.getEnumConstants()) {
			if (enumValue.getKey().equals(key)) {
				return enumValue;
			}
		}

		throw new IllegalArgumentException("Invalid key: " + key);
	}
}