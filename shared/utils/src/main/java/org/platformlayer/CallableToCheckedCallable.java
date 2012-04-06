package org.platformlayer;

import java.util.concurrent.Callable;

public class CallableToCheckedCallable<T> implements CheckedCallable<T, Exception>, Callable<T> {
    final Callable<T> inner;

    public static <T> CallableToCheckedCallable<T> build(Callable<T> inner) {
        return new CallableToCheckedCallable<T>(inner);
    }

    public CallableToCheckedCallable(Callable<T> inner) {
        super();
        this.inner = inner;
    }

    @Override
    public T call() throws Exception {
        return inner.call();
    }
}
