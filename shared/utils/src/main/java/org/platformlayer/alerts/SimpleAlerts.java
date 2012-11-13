package org.platformlayer.alerts;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class SimpleAlerts extends AlertsBase {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(SimpleAlerts.class);

	@Override
	protected void alert(AlertLevel level, String message, Exception e) {
		StringBuilder sb = new StringBuilder();
		sb.append("!!ALERT!!" + level.toString().toUpperCase() + "!!  " + message);
		if (e != null) {
			sb.append(e.toString());
		}

		log.warn(sb.toString());
	}
}
