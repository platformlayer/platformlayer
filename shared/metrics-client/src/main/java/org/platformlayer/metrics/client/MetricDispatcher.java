package org.platformlayer.metrics.client;

import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricProcessor;
import com.yammer.metrics.core.Timer;

/**
 * This is from the latest metrics source, but not yet incorporated into the released version
 */
public class MetricDispatcher {
	public static <T> void dispatch(Metric metric, MetricName name, MetricProcessor<T> processor, T context)
			throws Exception {
		if (metric instanceof Gauge) {
			processor.processGauge(name, (Gauge<?>) metric, context);
		} else if (metric instanceof Counter) {
			processor.processCounter(name, (Counter) metric, context);
		} else if (metric instanceof Meter) {
			processor.processMeter(name, (Meter) metric, context);
		} else if (metric instanceof Histogram) {
			processor.processHistogram(name, (Histogram) metric, context);
		} else if (metric instanceof Timer) {
			processor.processTimer(name, (Timer) metric, context);
		} else {
			throw new IllegalArgumentException("Unable to dispatch " + metric);
		}
	}
}