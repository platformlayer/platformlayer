package org.platformlayer.metrics;

import com.yammer.metrics.core.MetricName;

public class CodahaleMetricUtils {
	public static String sanitizeName(MetricName name) {
		final StringBuilder sb = new StringBuilder().append(name.getGroup()).append('.').append(name.getType())
				.append('.');
		if (name.hasScope()) {
			sb.append(name.getScope()).append('.');
		}
		return sb.append(name.getName()).toString();
	}
}
