package org.openstack.keystone.resources.user;

import java.util.Map;

import org.platformlayer.extensions.Extensions;
import org.platformlayer.extensions.HttpConfiguration;
import org.platformlayer.web.CORSFilter;

import com.google.common.collect.Maps;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

public class UserAuthServletModule extends JerseyServletModule {
	private final Extensions extensions;

	public UserAuthServletModule(Extensions extensions) {
		super();
		this.extensions = extensions;
	}

	@Override
	protected void configureServlets() {
		extensions.addHttpExtensions(new HttpConfiguration() {
			@Override
			public FilterKeyBindingBuilder filter(String urlPattern) {
				return UserAuthServletModule.this.filter(urlPattern);
			}

			@Override
			public ServletKeyBindingBuilder serve(String urlPattern) {
				return UserAuthServletModule.this.serve(urlPattern);
			}

			@Override
			public <T> AnnotatedBindingBuilder<T> bind(Class<T> clazz) {
				return UserAuthServletModule.this.bind(clazz);
			}
		});

		bind(CORSFilter.class).asEagerSingleton();
		filter("/api/*").through(CORSFilter.class);

		bind(RegisterResource.class);
		bind(PingResource.class);

		serve("/api/tokens").with(RestLoginServlet.class);

		Map<String, String> params = Maps.newHashMap();
		params.put(PackagesResourceConfig.PROPERTY_PACKAGES, "org.codehaus.jackson.jaxrs;com.fathomdb.jersey");
		serve("/*").with(GuiceContainer.class, params);
	}

}
