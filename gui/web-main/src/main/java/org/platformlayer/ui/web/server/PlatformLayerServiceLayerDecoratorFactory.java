package org.platformlayer.ui.web.server;

import org.platformlayer.ui.shared.server.inject.PlatformLayerServiceLayerDecorator;
import org.platformlayer.ui.shared.server.inject.ServiceLayerDecoratorFactory;

public class PlatformLayerServiceLayerDecoratorFactory extends ServiceLayerDecoratorFactory {

    @Override
    protected void addDecorators() {
        addDecorator(PlatformLayerServiceLayerDecorator.class);
        super.addDecorators();
    }

}
