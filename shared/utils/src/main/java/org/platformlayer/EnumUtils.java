package org.platformlayer;

import java.util.EnumSet;
import java.util.Enumeration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class EnumUtils {
	static final Logger log = LoggerFactory.getLogger(EnumUtils.class);

	public static <T> T getOnly(Enumeration<T> enumeration) {
		if (!enumeration.hasMoreElements()) {
			throw new IllegalStateException("No elements found in enumeration");
		}

		T retval = enumeration.nextElement();
		if (enumeration.hasMoreElements()) {
			throw new IllegalStateException("Found more than one element in enumeration");
		}

		return retval;
	}

	public static <T extends Enum<T>> List<T> getAllEnumValues(Class<T> enumType) {
		List<T> enumValues = Lists.newArrayList();

		for (T enumValue : EnumSet.allOf(enumType)) {
			enumValues.add(enumValue);
		}
		return enumValues;
	}

	public static <T extends Enum<T>> T valueOfCaseInsensitive(Class<T> enumType, String name) {
		if (name == null) {
			return null;
		}
		for (T enumValue : EnumSet.allOf(enumType)) {
			if (enumValue.toString().equalsIgnoreCase(name)) {
				return enumValue;
			}
		}
		throw new IllegalArgumentException("Unknown value for " + enumType + ": " + name);
	}

	public static <T extends Enum<T>> T valueOfOrNull(Class<T> enumType, String name, boolean checkCase) {
		if (name == null) {
			return null;
		}
		for (T enumValue : EnumSet.allOf(enumType)) {
			if (checkCase) {
				if (enumValue.toString().equals(name)) {
					return enumValue;
				}
			} else {
				if (enumValue.toString().equalsIgnoreCase(name)) {
					return enumValue;
				}
			}
		}
		return null;
	}
}
