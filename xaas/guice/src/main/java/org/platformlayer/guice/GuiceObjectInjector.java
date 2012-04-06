package org.platformlayer.guice;

import javax.inject.Singleton;

import org.platformlayer.inject.ObjectInjector;

import com.google.inject.Inject;
import com.google.inject.Injector;

@Singleton
public class GuiceObjectInjector implements ObjectInjector {

    @Inject
    Injector injector;

    @Override
    public <T> T getInstance(Class<T> clazz) {
        return injector.getInstance(clazz);
    }

    @Override
    public void injectMembers(Object o) {
        throw new UnsupportedOperationException();
    }

}
