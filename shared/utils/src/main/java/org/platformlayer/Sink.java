package org.platformlayer;

public interface Sink<T> {
    void apply(T arg);
}
