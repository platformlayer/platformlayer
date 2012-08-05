package org.platformlayer.jdbc;

import java.util.List;

import org.platformlayer.EnumUtils;

public class EnumWithKeys {

	public static <T extends EnumWithKey> T fromKey(Class<T> enumType, String key) {
		// This is naughty...
		Class c = enumType;
		List<T> enumValues = EnumUtils.getAllEnumValues(c);

		// TODO: Put into map?
		for (T enumValue : enumValues) {
			if (enumValue.getKey().equals(key)) {
				return enumValue;
			}
		}

		throw new IllegalArgumentException("Invalid key: " + key);
	}
}