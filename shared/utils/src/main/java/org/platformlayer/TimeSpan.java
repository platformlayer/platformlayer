package org.platformlayer;

import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

//@SerializedBy(TimeSpanSerializer.class)
//@UseConverter(TimeSpan.TimeSpanConverter.class)
public class TimeSpan implements Comparable<TimeSpan> {
	static final Logger log = Logger.getLogger(TimeSpan.class);

	public static final TimeSpan ONE_SECOND = new TimeSpan("1s", 1000);
	public static final TimeSpan TWO_SECONDS = new TimeSpan("2s", 2000);
	public static final TimeSpan THREE_SECONDS = new TimeSpan("3s", 3000);
	public static final TimeSpan FOUR_SECONDS = new TimeSpan("4s", 4000);
	public static final TimeSpan FIVE_SECONDS = new TimeSpan("5s", 5000);

	public static final TimeSpan TEN_SECONDS = new TimeSpan("10s", 10000);

	public static final TimeSpan ONE_MINUTE = new TimeSpan("1m", 60L * 1000L);
	public static final TimeSpan TWO_MINUTES = new TimeSpan("2m", 120L * 1000L);
	public static final TimeSpan FIVE_MINUTES = new TimeSpan("5m", 300L * 1000L);
	public static final TimeSpan TEN_MINUTES = new TimeSpan("10m", 600L * 1000L);
	public static final TimeSpan FIFTEEN_MINUTES = new TimeSpan("15m", 15L * 60L * 1000L);
	public static final TimeSpan THIRTY_MINUTES = new TimeSpan("30m", 30L * 60L * 1000L);

	public static final TimeSpan ONE_HOUR = new TimeSpan("1h", 60L * 60L * 1000L);

	public static final TimeSpan ONE_DAY = new TimeSpan("1d", 24L * 60L * 60L * 1000L);

	public static final TimeSpan ZERO = new TimeSpan("0s", 0);

	public static final TimeSpan ONE_YEAR_ROUGHLY = new TimeSpan("365d", 365L * 24L * 60L * 60L * 1000L);

	final long totalMilliseconds;
	final String stringRepresentation;

	// static class TimeSpanConverter implements Converter<TimeSpan> {
	// public TimeSpanConverter() {
	// }
	//
	// @Override
	// public TimeSpan convert(Class<TimeSpan> targetClass, Object sourceValue) {
	// if (sourceValue == null)
	// return null;
	// return new TimeSpan(sourceValue.toString());
	// }
	// }

	// public TimeSpan(long milliseconds) {
	// this.totalMilliseconds = milliseconds;
	// }

	public TimeSpan(String value) {
		this.stringRepresentation = value;

		// Milliseconds is the default, but that doesn't stop JSB trying to type it
		if (value.endsWith("ms")) {
			value = value.substring(0, value.length() - 2);
		}

		String remainder = value.substring(0, value.length() - 1);
		char lastChar = value.charAt(value.length() - 1);
		lastChar = Character.toLowerCase(lastChar);
		long multiplier = 1;
		switch (lastChar) {
		case 's':
			// Seconds
			multiplier = 1000L;
			break;
		case 'm':
			// Minutes
			multiplier = 60L * 1000L;
			break;
		case 'h':
			// Hours
			multiplier = 60L * 60L * 1000L;
			break;
		case 'd':
			// Days
			multiplier = 24L * 60L * 60L * 1000L;
			break;
		case 'w':
			// Weeks
			multiplier = 7L * 24L * 60L * 60L * 1000L;
			break;
		default:
			multiplier = 1;
			remainder = value;
			break;
		}
		long numValue = Long.parseLong(remainder);
		numValue *= multiplier;

		this.totalMilliseconds = numValue;
	}

	private TimeSpan(String stringRepresentation, long totalMilliseconds) {
		this.stringRepresentation = stringRepresentation;
		this.totalMilliseconds = totalMilliseconds;

		assert totalMilliseconds == new TimeSpan(stringRepresentation).getTotalMilliseconds();
	}

	public long getTotalMilliseconds() {
		return totalMilliseconds;
	}

	public long getTotalSeconds() {
		return totalMilliseconds / 1000;
	}

	public long getTotalMinutes() {
		return getTotalSeconds() / 60;
	}

	public long getTotalHours() {
		return getTotalMinutes() / 60;
	}

	public boolean isStillValid(Date createdAt) {
		if (createdAt == null) {
			return false;
		}

		return isStillValid(createdAt.getTime());
	}

	public boolean isStillValid(long createdAt) {
		if (createdAt == 0) {
			return false;
		}

		return !hasTimedOut(createdAt);
	}

	public boolean isStillValid(Calendar createdAt) {
		if (createdAt == null) {
			return false;
		}

		return isStillValid(createdAt.getTimeInMillis());
	}

	public boolean hasTimedOut(long start) {
		long now = System.currentTimeMillis();
		long end = start + getTotalMilliseconds();
		return (end < now);
	}

	public boolean hasTimedOut(Date date) {
		return hasTimedOut(date.getTime());
	}

	public boolean isWithinSkewWindow(long timestamp) {
		long now = System.currentTimeMillis();
		long skew = Math.abs(timestamp - now);
		return skew < getTotalMilliseconds();
	}

	@Override
	public String toString() {
		return stringRepresentation;
	}

	@Deprecated
	public void unsafeSleep() {
		try {
			sleep();
		} catch (InterruptedException e) {
			log.debug("Ignoring InterruptedException", e);
		}
	}

	public void sleep() throws InterruptedException {
		Thread.sleep(getTotalMilliseconds());
	}

	public boolean doSafeSleep() {
		try {
			sleep();
			return true;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt(); // Propogate signal onwards and upwards
			return false;
		}
	}

	public Date addTo(Date date) {
		Date added = new Date(date.getTime() + this.getTotalMilliseconds());
		return added;
	}

	public Date subtractFrom(Date date) {
		Date subtracted = new Date(date.getTime() - this.getTotalMilliseconds());
		return subtracted;
	}

	public static TimeSpan fromSeconds(long seconds) {
		if (seconds <= 5) {
			switch ((int) seconds) {
			case 0:
				return TimeSpan.ZERO;
			case 1:
				return TimeSpan.ONE_SECOND;
			case 2:
				return TimeSpan.TWO_SECONDS;
			case 3:
				return TimeSpan.THREE_SECONDS;
			case 4:
				return TimeSpan.FOUR_SECONDS;
			case 5:
				return TimeSpan.FIVE_SECONDS;
			}
		}

		long milliseconds = seconds * 1000L;
		if ((seconds % 60L) != 0) {
			return new TimeSpan(seconds + "s", milliseconds);
		}

		if ((seconds % 3600L) != 0) {
			return new TimeSpan((seconds / 60L) + "m", milliseconds);
		}

		return new TimeSpan((seconds / 3600L) + "h", milliseconds);
	}

	public static TimeSpan fromMilliseconds(long milliseconds) {
		if ((milliseconds % 1000L) != 0) {
			return new TimeSpan(Long.toString(milliseconds), milliseconds);
		}

		return fromSeconds(milliseconds / 1000L);
	}

	public boolean isLessThan(TimeSpan compare) {
		return this.getTotalMilliseconds() < compare.getTotalMilliseconds();
	}

	public boolean isGreaterThan(TimeSpan compare) {
		return this.getTotalMilliseconds() > compare.getTotalMilliseconds();
	}

	public Date truncate(Date date) {
		long ticks = date.getTime();
		return new Date(truncate(ticks));
	}

	public long truncate(long ticks) {
		ticks -= ticks % this.getTotalMilliseconds();
		return ticks;
	}

	public static TimeSpan timeTillNow(Date startTime) {
		return TimeSpan.fromMilliseconds(System.currentTimeMillis() - startTime.getTime());
	}

	public static TimeSpan timeUntil(Date nextTime) {
		return TimeSpan.fromMilliseconds(nextTime.getTime() - System.currentTimeMillis());
	}

	public double divideBy(TimeSpan divisor) {
		return ((double) this.getTotalMilliseconds()) / ((double) divisor.getTotalMilliseconds());
	}

	public static TimeSpan add(TimeSpan a, TimeSpan b) {
		return TimeSpan.fromMilliseconds(a.getTotalMilliseconds() + b.getTotalMilliseconds());
	}

	public TimeSpan multiplyBy(double scaleFactor) {
		return TimeSpan.fromMilliseconds((long) (this.getTotalMilliseconds() * scaleFactor));
	}

	public String toPrettyString() {
		if (isLessThan(ONE_SECOND)) {
			return getTotalMilliseconds() + "ms";
		}

		if (isLessThan(ONE_MINUTE)) {
			return getTotalSeconds() + "s";
		}

		if (isLessThan(ONE_HOUR)) {
			return getTotalMinutes() + "m";
		}

		return getTotalHours() + "h" + " " + (getTotalMinutes() % 60) + "m";
	}

	public static TimeSpan subtract(Date end, Date start) {
		return TimeSpan.fromMilliseconds(end.getTime() - start.getTime());
	}

	public boolean isMultipleOf(TimeSpan checkDivisor) {
		long checkMilliseconds = checkDivisor.getTotalMilliseconds();
		long thisMilliseconds = this.getTotalMilliseconds();

		return (thisMilliseconds % checkMilliseconds) == 0;
	}

	public static TimeSpan max(TimeSpan a, TimeSpan b) {
		if (a.getTotalMilliseconds() >= b.getTotalMilliseconds()) {
			return a;
		}
		return b;
	}

	public static TimeSpan min(TimeSpan a, TimeSpan b) {
		if (a.getTotalMilliseconds() <= b.getTotalMilliseconds()) {
			return a;
		}
		return b;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (totalMilliseconds ^ (totalMilliseconds >>> 32));
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
		TimeSpan other = (TimeSpan) obj;
		if (totalMilliseconds != other.totalMilliseconds) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(TimeSpan that) {
		return PrimitiveComparators.compare(this.totalMilliseconds, that.totalMilliseconds);
	}

	public boolean isZero() {
		return this.totalMilliseconds == 0;
	}

	public Calendar subtractFrom(Calendar from) {
		long time = from.getTimeInMillis();
		time -= this.totalMilliseconds;
		Calendar copy = (Calendar) from.clone();
		copy.setTimeInMillis(time);
		return copy;
	}

	public static TimeSpan parse(String s) {
		return new TimeSpan(s);
	}

}
