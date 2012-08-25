package org.openstack.keystone.server;

import java.util.EnumSet;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.jetty.server.Server;
import org.openstack.keystone.resources.admin.KeychainResource;
import org.openstack.keystone.resources.admin.PkiResource;
import org.openstack.keystone.resources.admin.TokensResource;
import org.platformlayer.WellKnownPorts;
import org.platformlayer.auth.server.GuiceAuthenticationConfig;
import org.platformlayer.metrics.MetricsSystem;
import org.platformlayer.web.SslOption;
import org.platformlayer.web.WebServerBuilder;

import com.google.common.collect.Maps;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

public class KeystoneAdminServer {
	private Server jettyServer;

	@Inject
	WebServerBuilder serverBuilder;

	@Inject
	Injector injector;

	@Inject
	MetricsSystem metricsSystem;

	public static void main(String[] args) throws Exception {
		Injector injector = Guice.createInjector(new GuiceAuthenticationConfig(), new AdminGuiceBindings(),
				new JerseyServletModule() {
					@Override
					protected void configureServlets() {
						bind(TokensResource.class);
						bind(KeychainResource.class);
						bind(PkiResource.class);

						Map<String, String> params = Maps.newHashMap();
						params.put(PackagesResourceConfig.PROPERTY_PACKAGES,
								"org.openstack.keystone.jaxrs;org.codehaus.jackson.jaxrs");
						serve("/*").with(GuiceContainer.class, params);
					}
				});

		KeystoneAdminServer server = injector.getInstance(KeystoneAdminServer.class);
		server.start(WellKnownPorts.PORT_PLATFORMLAYER_AUTH_ADMIN);
	}

	public void start(int port) throws Exception {
		EnumSet<SslOption> options = EnumSet.of(SslOption.AllowAnyClientCertificate, SslOption.WantClientCertificate);

		serverBuilder.addHttpsConnector(port, options);
		serverBuilder.addGuiceContext("/", injector);

		this.jettyServer = serverBuilder.start();

		metricsSystem.init();
	}

	public void stop() throws Exception {
		if (jettyServer != null) {
			jettyServer.stop();
		}
	}
}
