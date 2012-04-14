package org.platformlayer;

import org.apache.log4j.Logger;

public class ByteCount implements Comparable<ByteCount> {
	static final Logger log = Logger.getLogger(ByteCount.class);

	static final long BYTES_IN_KILOBYTE = 1024L;
	static final long BYTES_IN_MEGABYTE = 1024L * 1024L;
	static final long BYTES_IN_GIGABYTE = 1024L * 1024L * 1024L;

	public static final ByteCount ONE_KB = new ByteCount(BYTES_IN_KILOBYTE, "1k");

	public static final ByteCount ONE_MB = new ByteCount(BYTES_IN_MEGABYTE, "1m");

	public static final ByteCount ONE_GB = new ByteCount(BYTES_IN_GIGABYTE, "1g");

	public static final ByteCount ZERO = new ByteCount(0, "0");

	final long totalBytes;
	final String stringRepresentation;

	public ByteCount(long totalBytes) {
		this.totalBytes = totalBytes;
		this.stringRepresentation = buildStringRepresentation(totalBytes);
	}

	private String buildStringRepresentation(long totalBytes) {
		return buildJavaStringRepresentation(totalBytes);
	}

	public ByteCount(String value) {
		this.stringRepresentation = value;

		String remainder = value.substring(0, value.length() - 1);
		char lastChar = value.charAt(value.length() - 1);
		lastChar = Character.toLowerCase(lastChar);
		long multiplier = 1;
		switch (lastChar) {
		case 'k':
			// KB
			multiplier = BYTES_IN_KILOBYTE;
			break;
		case 'm':
			// MB
			multiplier = BYTES_IN_MEGABYTE;
			break;
		case 'g':
			// Gig
			multiplier = BYTES_IN_GIGABYTE;
			break;
		default:
			multiplier = 1;
			remainder = value;
			break;
		}
		long numValue = Long.parseLong(remainder);
		numValue *= multiplier;

		this.totalBytes = numValue;
	}

	private ByteCount(long totalBytes, String stringRepresentation) {
		this.totalBytes = totalBytes;
		this.stringRepresentation = stringRepresentation;
	}

	public long getTotalBytes() {
		return totalBytes;
	}

	public long getTotalKB() {
		return totalBytes / BYTES_IN_KILOBYTE;
	}

	public long getTotalMB() {
		return totalBytes / BYTES_IN_MEGABYTE;
	}

	public long getTotalGB() {
		return totalBytes / BYTES_IN_GIGABYTE;
	}

	@Override
	public String toString() {
		return stringRepresentation;
	}

	public String getJavaArgFormat() {
		long totalBytes = getTotalBytes();
		return buildJavaStringRepresentation(totalBytes);
	}

	private static String buildJavaStringRepresentation(long totalBytes) {
		if ((totalBytes % BYTES_IN_GIGABYTE) == 0) {
			return totalBytes / BYTES_IN_GIGABYTE + "g";
		}

		if ((totalBytes % BYTES_IN_MEGABYTE) == 0) {
			return totalBytes / BYTES_IN_MEGABYTE + "m";
		}

		if ((totalBytes % BYTES_IN_KILOBYTE) == 0) {
			return totalBytes / BYTES_IN_KILOBYTE + "k";
		}

		return totalBytes + "";
	}

	public static ByteCount parseJavaFormat(String value) {
		return new ByteCount(value);
	}

	public ByteCount multiplyBy(double scaleFactor) {
		return new ByteCount((long) (getTotalBytes() * scaleFactor));
	}

	public boolean isZero() {
		return totalBytes == 0;
	}

	public static ByteCount min(ByteCount a, ByteCount b) {
		if (a.getTotalBytes() <= b.getTotalBytes()) {
			return a;
		}
		return b;
	}

	public ByteCount minus(ByteCount b) {
		return new ByteCount(this.getTotalBytes() - b.getTotalBytes());
	}

	public ByteCount add(ByteCount b) {
		return new ByteCount(this.getTotalBytes() + b.getTotalBytes());
	}

	public static ByteCount fromGigabytes(int gigabytes) {
		long totalBytes = BYTES_IN_GIGABYTE * gigabytes;
		return new ByteCount(totalBytes, gigabytes + "g");
	}

	public static ByteCount fromMegabytes(long megabytes) {
		long totalBytes = BYTES_IN_MEGABYTE * megabytes;
		return new ByteCount(totalBytes, megabytes + "m");
	}

	public static ByteCount fromKB(long kilobytes) {
		long totalBytes = BYTES_IN_KILOBYTE * kilobytes;
		return new ByteCount(totalBytes, kilobytes + "k");
	}

	public String smartFormat() {
		if (totalBytes < BYTES_IN_KILOBYTE) {
			return totalBytes + " bytes";
		}

		if (totalBytes < BYTES_IN_MEGABYTE) {
			return (totalBytes / BYTES_IN_KILOBYTE) + " k";
		}

		if (totalBytes < BYTES_IN_GIGABYTE) {
			return (totalBytes / BYTES_IN_MEGABYTE) + " m";
		}

		return (totalBytes / BYTES_IN_GIGABYTE) + " g";
	}

	@Override
	public int compareTo(ByteCount o) {
		return PrimitiveComparators.compare(this.totalBytes, o.totalBytes);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (totalBytes ^ (totalBytes >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ByteCount other = (ByteCount) obj;
		if (totalBytes != other.totalBytes) {
			return false;
		}
		return true;
	}

}
