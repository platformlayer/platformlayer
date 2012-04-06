package org.platformlayer.ui.web.client;

import java.util.Map;

import org.platformlayer.ui.shared.client.commons.BasicInjector;
import org.platformlayer.ui.shared.client.commons.HttpRequestTransport;
import org.platformlayer.ui.web.client.PlatformLayerInjector.PlatformLayerBindings;
import org.platformlayer.ui.web.client.widgets.MainPanel;
import org.platformlayer.ui.web.shared.PlatformLayerRequestFactory;

import com.google.gwt.core.client.GWT;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;

@GinModules({ PlatformLayerBindings.class })
public interface PlatformLayerInjector extends Ginjector, BasicInjector {
    MainPanel getMainPanel();

    PlatformLayerRequestFactory getRequestFactory();

    public class PlatformLayerBindings extends AbstractGinModule {
        @Override
        protected void configure() {
            bind(MainPanel.class).in(Singleton.class);
            bind(EventBus.class).to(SimpleEventBus.class).in(Singleton.class);
        }

        @Provides
        @Singleton
        public PlatformLayerRequestFactory createRequestFactory(EventBus eventBus) {
            PlatformLayerRequestFactory requests = GWT.create(PlatformLayerRequestFactory.class);

            String baseUrl = GWT.getModuleBaseURL();
            baseUrl = baseUrl.replace("/static/platformlayerui/", "/");

            String url = baseUrl + "gwtRequest";

            HttpRequestTransport transport = new HttpRequestTransport();
            transport.setRequestUrl(url);

            Map<String, String> headers = transport.getHeaders();
            headers.put("X-Auth-Token", "DEV-TOKEN-fathomdb");

            requests.initialize(eventBus, transport);

            return requests;
        }
    }

}
