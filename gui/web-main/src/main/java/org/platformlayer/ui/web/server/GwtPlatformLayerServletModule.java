package org.platformlayer.ui.web.server;

import org.openstack.keystone.service.OpenstackAuthenticationFilterBase;
import org.platformlayer.ui.shared.server.inject.InjectedRequestFactoryServlet;
import org.platformlayer.ui.shared.server.inject.ServiceLayerDecoratorFactory;
import org.platformlayer.xaas.PlatformLayerServletModule;

import com.google.web.bindery.requestfactory.server.DefaultExceptionHandler;
import com.google.web.bindery.requestfactory.server.ExceptionHandler;

public class GwtPlatformLayerServletModule extends PlatformLayerServletModule {
    @Override
    protected void configureServlets() {
        bind(ExceptionHandler.class).to(DefaultExceptionHandler.class);
        bind(ServiceLayerDecoratorFactory.class).to(PlatformLayerServiceLayerDecoratorFactory.class);

        // Map<String, String> params = Maps.newHashMap();
        // <init-param>
        // <param-name>symbolMapsDirectory</param-name>
        // <!-- You'll need to compile with -extras and move the symbolMaps directory
        // to this location if you want stack trace deobfuscation to work -->
        // <param-value>WEB-INF/classes/symbolMaps/</param-value>
        // </init-param>
        bind(InjectedRequestFactoryServlet.class).asEagerSingleton();
        serve("/gwtRequest").with(InjectedRequestFactoryServlet.class);

        filter("/gwtRequest").through(OpenstackAuthenticationFilterBase.class);

        bind(ServerContextFilter.class).asEagerSingleton();

        filter("/gwtRequest").through(ServerContextFilter.class);

        bind(DefaultWrapperServlet.class).asEagerSingleton();
        serve("/static/*").with(DefaultWrapperServlet.class);

        super.configureServlets();
    }
}
