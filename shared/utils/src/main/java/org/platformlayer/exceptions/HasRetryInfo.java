package org.platformlayer.exceptions;

import org.platformlayer.TimeSpan;

public interface HasRetryInfo {
	public TimeSpan getRetry();
}
