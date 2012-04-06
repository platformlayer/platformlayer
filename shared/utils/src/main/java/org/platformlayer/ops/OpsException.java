package org.platformlayer.ops;

import org.platformlayer.TimeSpan;
import org.platformlayer.exceptions.ExceptionHelpers;
import org.platformlayer.exceptions.HasRetryInfo;

public class OpsException extends Exception implements HasRetryInfo {
    private static final long serialVersionUID = 1L;

    TimeSpan retry;

    public OpsException() {
        super();
    }

    public OpsException(String message, Throwable cause) {
        super(message, cause);
        copyRetryInfo(cause);
    }

    public OpsException(String message) {
        super(message);
    }

    public OpsException(Throwable cause) {
        super(cause);
        copyRetryInfo(cause);
    }

    protected void copyRetryInfo(Throwable cause) {
        HasRetryInfo retryInfo = ExceptionHelpers.findRetryInfo(cause);
        if (retryInfo != null) {
            this.retry = retryInfo.getRetry();
        }
    }

    @Override
    public TimeSpan getRetry() {
        return retry;
    }

    public OpsException setRetry(TimeSpan retry) {
        this.retry = retry;
        return this;
    }

}
