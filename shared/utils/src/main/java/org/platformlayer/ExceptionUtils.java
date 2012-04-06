package org.platformlayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionUtils {
    static final Logger LOG = LoggerFactory.getLogger(ExceptionUtils.class);

    @SuppressWarnings("unchecked")
    public static <T extends Throwable> T findExceptionInChain(Class<T> clazz, Throwable e) {
        Throwable current = e;
        while (current != null) {
            if (clazz.isAssignableFrom(current.getClass())) {
                return (T) current;
            }

            Throwable cause = current.getCause();
            if (cause == current) {
                break;
            }

            current = cause;
        }
        return null;
    }

    public static void handleInterrupted(Throwable t) {
        if (t instanceof InterruptedException) {
            LOG.info("Caught interrupted exception, marking thread interrupted");
            Thread.currentThread().interrupt();
        }
    }
}
