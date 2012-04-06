package org.platformlayer.exceptions;

public class ExceptionHelpers {
    public static HasRetryInfo findRetryInfo(Throwable e) {
        if (e instanceof HasRetryInfo) {
            return (HasRetryInfo) e;
        }

        if (e.getCause() != null)
            return findRetryInfo(e.getCause());

        return null;
    }
}
