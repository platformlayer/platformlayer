package org.platformlayer.alerts;

import com.google.inject.ImplementedBy;

/**
 * Alerts for things that should typically be raised to an operator
 */
@ImplementedBy(SimpleAlerts.class)
public interface Alerts {

	void critical(String message);

	void critical(String message, Exception e);

	void info(String message);

	void info(String message, Exception e);

}
