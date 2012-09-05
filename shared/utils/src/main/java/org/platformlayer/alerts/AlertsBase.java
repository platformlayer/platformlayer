package org.platformlayer.alerts;

import org.apache.log4j.Logger;

public abstract class AlertsBase implements Alerts {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(AlertsBase.class);

	@Override
	public void critical(String message) {
		alert(AlertLevel.Critical, message, null);
	}

	@Override
	public void critical(String message, Exception e) {
		alert(AlertLevel.Critical, message, e);
	}

	@Override
	public void info(String message) {
		alert(AlertLevel.Info, message, null);
	}

	@Override
	public void info(String message, Exception e) {
		alert(AlertLevel.Info, message, e);
	}

	protected abstract void alert(AlertLevel level, String message, Exception e);
}
