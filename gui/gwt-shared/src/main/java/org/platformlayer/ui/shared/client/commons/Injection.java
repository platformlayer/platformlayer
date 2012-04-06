package org.platformlayer.ui.shared.client.commons;

public class Injection {
    private static BasicInjector INJECTOR;

    public static BasicInjector injector() {
        return INJECTOR;
    }
}
