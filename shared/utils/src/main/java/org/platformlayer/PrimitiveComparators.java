package org.platformlayer;

public class PrimitiveComparators {
	public static int compare(long l, long r) {
		return (l < r ? -1 : (l == r ? 0 : 1));
	}

	public static int compare(int l, int r) {
		return (l < r ? -1 : (l == r ? 0 : 1));
	}

	public static int compare(short l, short r) {
		return (l < r ? -1 : (l == r ? 0 : 1));
	}

	public static int compare(byte l, byte r) {
		return (l < r ? -1 : (l == r ? 0 : 1));
	}

	public static int compare(double l, double r) {
		return (l < r ? -1 : (l == r ? 0 : 1));
	}
}
