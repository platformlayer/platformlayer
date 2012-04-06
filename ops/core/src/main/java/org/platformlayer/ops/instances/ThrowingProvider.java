package org.platformlayer.ops.instances;

import javax.inject.Provider;

public abstract class ThrowingProvider<S> implements Provider<S> {
    public abstract S build() throws Exception;

    public S get() {
        try {
            return build();
        } catch (Exception e) {
            throw new IllegalArgumentException("Error building object in provider", e);
        }
    }
}
