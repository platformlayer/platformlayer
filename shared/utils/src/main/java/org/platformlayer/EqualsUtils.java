package org.platformlayer;

import com.google.common.base.Objects;

public class EqualsUtils {
	// public static void checkEquals(Object expected, Object actual) {
	// checkEquals(expected, actual, null);
	// }
	//
	// public static void checkEquals(Object expected, Object actual, String message) {
	// if (equals(expected, actual)) {
	// return;
	// }
	//
	// if (message == null) {
	// message = "Unexpected value";
	// }
	// throw new IllegalStateException(message + ": expected=" + expected + " actual=" + actual);
	// }
	//
	// public interface EqualityTester<T> {
	// boolean equals(T left, T right);
	// }
	//
	// @SuppressWarnings("unchecked")
	// public static <T> boolean equals(T left, Object compare, EqualityTester<T> tester) {
	// if (left == compare) {
	// return true;
	// }
	// if (compare == null) {
	// return false;
	// }
	// if (left.getClass() != compare.getClass()) {
	// return false;
	// }
	// final T right = (T) compare;
	// return tester.equals(left, right);
	// }
	//
	// public static boolean equals(String left, String right) {
	// if (left == right) {
	// return true;
	// }
	// if (left == null) {
	// return false;
	// }
	// return left.equals(right);
	// }
	//
	// public static boolean equalsIgnoreCase(String left, String right) {
	// if (left == right) {
	// return true;
	// }
	// if (left == null) {
	// return false;
	// }
	// return left.equalsIgnoreCase(right);
	// }
	//
	// public static boolean equals(Object left, Object right) {
	// if (left == right) {
	// return true;
	// }
	// if (left == null) {
	// return false;
	// }
	// return left.equals(right);
	// }
	//
	// public static int safeCompare(String left, String right) {
	// if (left == null) {
	// if (right == null) {
	// return 0;
	// } else {
	// return -1;
	// }
	// }
	//
	// if (right == null) {
	// return 1;
	// }
	//
	// return left.compareTo(right);
	// }

	public static int computeHashCode(HasIdentityValues hasIdentityValues) {
		final int prime = 31;
		int result = 1;
		Object[] identityValues = hasIdentityValues.getIdentityValues();
		for (int i = 0; i < identityValues.length; i++) {
			Object value = identityValues[i];

			result = prime * result + ((value == null) ? 0 : value.hashCode());
		}
		return result;
	}

	public static boolean equals(HasIdentityValues thisObj, Object compareObj) {
		if (thisObj == compareObj) {
			return true;
		}
		if (compareObj == null) {
			return false;
		}
		if (thisObj.getClass() != compareObj.getClass()) {
			return false;
		}
		HasIdentityValues other = (HasIdentityValues) compareObj;
		Object[] thisValues = thisObj.getIdentityValues();
		Object[] otherValues = other.getIdentityValues();
		if (thisValues.length != otherValues.length) {
			return false;
		}
		for (int i = 0; i < thisValues.length; i++) {
			if (!Objects.equal(thisValues[i], otherValues[i])) {
				return false;
			}
		}
		return true;
	}
}
