package org.platformlayer.ops.schedule;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import org.platformlayer.TimeSpan;

public class SimpleJobScheduleCalculator implements JobScheduleCalculator {
	public static final Date SUGGESTED_BASE = new Date(2000 - 1900, 0, 1, 0, 0, 0);

	public final Date base;
	public final TimeSpan interval;

	static final double MIN_RANDOM_FRACTION = 0.2;
	static final Random random = new Random();

	static final TimeSpan TIME_SKEW_ALLOWANCE = new TimeSpan("15s");

	public SimpleJobScheduleCalculator(TimeSpan interval, Date base) {
		this.base = base;

		this.interval = interval;
		if (interval == null) {
			throw new IllegalArgumentException("Interval not specified.  Task=" + this);
		}
	}

	@Override
	public String toString() {
		String intervalString;

		if (interval.getTotalSeconds() == TimeSpan.ONE_DAY.getTotalSeconds()) {
			intervalString = " Daily";
			SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss Z", Locale.US);

			return dateFormat.format(base) + intervalString;
		} else if (interval.getTotalSeconds() == 7 * TimeSpan.ONE_DAY.getTotalSeconds()) {
			intervalString = " Weekly";
			SimpleDateFormat dateFormat = new SimpleDateFormat("EEE HH:mm:ss Z", Locale.US);

			return dateFormat.format(base) + intervalString;
		}

		intervalString = "Every " + interval.toPrettyString();

		if (base == null || SUGGESTED_BASE.equals(base)) {
			return intervalString;
		} else {
			return intervalString + " from " + base;
		}
	}

	public TimeSpan getInterval() {
		return interval;
	}

	public Date getBase() {
		return base;
	}

	protected static Date calculateNext(final Date base, final TimeSpan previousInterval,
			final TimeSpan currentInterval, final JobExecution previousExecution) {
		long now = System.currentTimeMillis();

		if (base == null) {
			// Unbased
			if (previousExecution == null) {
				// We avoid randomly choosing a really small fraction, as that tends to start up tasks before the system
				// is really ready
				double fraction;
				do {
					synchronized (random) {
						fraction = random.nextDouble();
					}
				} while (fraction < MIN_RANDOM_FRACTION);

				TimeSpan delay = TimeSpan.fromMilliseconds((long) (fraction * previousInterval.getTotalMilliseconds()));
				return new Date(now + delay.getTotalMilliseconds());
			} else {
				return currentInterval.addTo(previousExecution.getStartTimestamp());
			}
		} else {
			// Based e.g. every morning at 2AM
			long partial = (now - base.getTime()) % previousInterval.getTotalMilliseconds();
			long previous = now - partial;
			long next = previous + currentInterval.getTotalMilliseconds();

			if (previousExecution != null) {
				// Check the last execution against what we think the last execution should have been (with a bit of
				// skew tolerance)
				if (previousExecution.getStartTimestamp().getTime() <= (previous - TIME_SKEW_ALLOWANCE
						.getTotalMilliseconds())) {
					// We missed our previous execution, we should run asap

					// Introduce a random delay to stop the thundering herd
					long delay = (long) (random.nextDouble() * previousInterval.getTotalMilliseconds() / 10);
					long scheduleAt = now + delay;

					// Check that we're not going to schedule it just before the next run anyway
					// Just before = 1/10 th. So for a daily task running at 4AM, if it's later than
					// 2.4 hours before = 1:36AM. That seems fairly sensible.
					if (scheduleAt < (next - previousInterval.getTotalMilliseconds() / 10)) {
						return new Date(scheduleAt);
					}
				}
			}

			return new Date(next);
		}
	}

	@Override
	public Date calculateNext(JobExecution previousExecution) {
		return calculateNext(base, interval, interval, previousExecution);
	}
}