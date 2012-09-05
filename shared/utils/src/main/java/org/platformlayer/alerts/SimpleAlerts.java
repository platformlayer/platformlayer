package org.platformlayer.alerts;

import javax.inject.Singleton;

import org.apache.log4j.Logger;

@Singleton
public class SimpleAlerts extends AlertsBase {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(SimpleAlerts.class);

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
