package org.platformlayer.exceptions;

import com.fathomdb.TimeSpan;

public interface HasRetryInfo {
	public TimeSpan getRetry();
}
