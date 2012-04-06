package org.platformlayer.ui.web.server;

import com.google.web.bindery.requestfactory.shared.ServiceLocator;

public class ServerContextServiceLocator implements ServiceLocator {

    public Object getInstance(Class<?> clazz) {
        ServerContext gwtContext = ServerContext.get();
        return gwtContext.getInstance(clazz);
    }

}
