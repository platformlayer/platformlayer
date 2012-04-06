package org.platformlayer.ui.web.client;

import org.platformlayer.ui.shared.client.commons.Injection;

import com.google.gwt.core.client.GWT;

public class App {
    public static final PlatformLayerInjector injector = GWT.create(PlatformLayerInjector.class);

    static {
        Injection.injector = App.injector;
    }
}
