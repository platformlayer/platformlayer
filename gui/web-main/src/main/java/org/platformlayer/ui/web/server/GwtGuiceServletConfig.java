package org.platformlayer.ui.web.server;

import java.util.List;

import org.platformlayer.xaas.GuiceServletConfig;

import com.google.inject.Module;

public class GwtGuiceServletConfig extends GuiceServletConfig {

    @Override
    protected void addServletModule(List<Module> modules) {
        modules.add(new GwtPlatformLayerServletModule());
    }
}
