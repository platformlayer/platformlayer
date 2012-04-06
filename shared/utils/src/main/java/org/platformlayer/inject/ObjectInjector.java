package org.platformlayer.inject;

public interface ObjectInjector {
    <T> T getInstance(Class<T> clazz);

    void injectMembers(Object o);
}
