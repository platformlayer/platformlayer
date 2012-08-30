package org.openstack.keystone.server;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.jetty.server.Server;
import org.openstack.keystone.resources.admin.KeychainResource;
import org.openstack.keystone.resources.admin.PkiResource;
import org.openstack.keystone.resources.admin.TokensResource;
import org.platformlayer.WellKnownPorts;
import org.platformlayer.auth.KeystoneJdbcModule;
import org.platformlayer.auth.keystone.KeystoneOpsSystemModule;
import org.platformlayer.auth.server.GuiceAuthenticationConfig;
import org.platformlayer.cache.CacheModule;
import org.platformlayer.config.ConfigurationModule;
import org.platformlayer.metrics.MetricReporter;
import org.platformlayer.metrics.client.codahale.CodahaleMetricsModule;
import org.platformlayer.web.SslOption;
import org.platformlayer.web.WebServerBuilder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
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
	MetricReporter metricReporter;

	public static void main(String[] args) throws Exception {
		List<Module> modules = Lists.newArrayList();
		modules.add(new ConfigurationModule());
		modules.add(new CacheModule());
		modules.add(new GuiceAuthenticationConfig());
		modules.add(new KeystoneJdbcModule());
		modules.add(new KeystoneOpsSystemModule());
		modules.add(new CodahaleMetricsModule());
		modules.add(new JerseyServletModule() {
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

		Injector injector = Guice.createInjector(modules);

		KeystoneAdminServer server = injector.getInstance(KeystoneAdminServer.class);
		server.start(WellKnownPorts.PORT_PLATFORMLAYER_AUTH_ADMIN);
	}

	public void start(int port) throws Exception {
		EnumSet<SslOption> options = EnumSet.of(SslOption.AllowAnyClientCertificate, SslOption.WantClientCertificate);

		serverBuilder.addHttpsConnector(port, options);
		serverBuilder.addGuiceContext("/", injector);

		this.jettyServer = serverBuilder.start();

		metricReporter.start();
	}

	public void stop() throws Exception {
		if (jettyServer != null) {
			jettyServer.stop();
		}
	}
}
