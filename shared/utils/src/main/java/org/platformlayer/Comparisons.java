package org.platformlayer;


public class Comparisons {
	public static boolean equalsIgnoreCase(String a, String b) {
		if (a == b) {
			return true;
		}
		if (a == null || b == null) {
			return false;
		}

		return a.equalsIgnoreCase(b);
	}
}
