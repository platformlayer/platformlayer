package org.platformlayer;

public class Strings {
	/**
	 * Split a string based on a literal. Useful because we don't really want regexes normally, and because J2ME doesn't
	 * have String.split
	 */
	public static String[] splitLiteral(String s, String separator) {
		// We use a two pass algorithm to avoid the memory copy
		int count = 0;
		int start = 0;
		while (true) {
			int end = s.indexOf(separator, start);
			if (end < 0) {
				count++;
				break;
			}

			count++;
			start = end + separator.length();

			// if (start >= s.length())
			// break;
		}

		String[] result = new String[count];
		count = 0;
		start = 0;
		while (true) {
			int end = s.indexOf(separator, start);
			if (end < 0) {
				result[count] = s.substring(start);
				count++;
				break;
			}

			result[count] = s.substring(start, end);
			count++;
			start = end + separator.length();

			// if (start >= s.length())
			// break;
		}

		return result;
	}

	/**
	 * Split a string based on a literal. Useful because we don't really want regexes normally, and because J2ME doesn't
	 * have String.split
	 */
	public static String[] splitLiteral(String s, char separator) {
		// We use a two pass algorithm to avoid the memory copy
		int count = 0;
		int start = 0;
		while (true) {
			int end = s.indexOf(separator, start);
			if (end < 0) {
				count++;
				break;
			}

			count++;
			start = end + 1;

			// if (start >= s.length())
			// break;
		}

		String[] result = new String[count];
		count = 0;
		start = 0;
		while (true) {
			int end = s.indexOf(separator, start);

			if (end < 0) {
				result[count] = s.substring(start);
				count++;
				break;
			}
			result[count] = s.substring(start, end);
			count++;
			start = end + 1;

			// if (start >= s.length())
			// break;
		}

		return result;
	}

	public static String replaceLiteral(String text, String find, String replace) {
		StringBuilder replaced = null;
		int startIndex = 0;
		while (true) {
			int foundAt = text.indexOf(find, startIndex);
			if (foundAt == -1) {
				if (replaced == null) {
					return text;
				}
				replaced.append(text.substring(startIndex));
				return replaced.toString();
			}

			if (replaced == null) {
				replaced = new StringBuilder();
			}

			replaced.append(text.substring(startIndex, foundAt));
			replaced.append(replace);
			startIndex = foundAt + find.length();
		}
	}

	public static boolean equalsIgnoreCase(String a, String b) {
		if (a == b) {
			return true;
		}
		if (a == null || b == null) {
			return false;
		}

		return a.equalsIgnoreCase(b);
	}

	public static boolean isEmpty(String s) {
		return (s == null || s.isEmpty());
	}
}
