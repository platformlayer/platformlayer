package org.openstack.keystone.server;

import java.util.Map;

import org.openstack.keystone.resources.admin.TokensResource;

import com.google.common.collect.Maps;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

public class AdminServerConfig extends GuiceServletContextListener {

	@Override
	protected Injector getInjector() {
		return Guice.createInjector(new GuiceAuthenticationConfig(), new JerseyServletModule() {
			@Override
			protected void configureServlets() {

				boolean isAdmin = false;
				if (isAdmin) {
					throw new UnsupportedOperationException();
				} else {
					bind(TokensResource.class);

					Map<String, String> params = Maps.newHashMap();
					params.put(PackagesResourceConfig.PROPERTY_PACKAGES,
							"org.openstack.keystone.jaxrs;org.codehaus.jackson.jaxrs");
					serve("/*").with(GuiceContainer.class, params);
				}
			}
		});
	}
}
