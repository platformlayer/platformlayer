package org.openstack.keystone.server;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.jetty.server.Server;
import org.openstack.keystone.resources.user.RegisterResource;
import org.openstack.keystone.resources.user.TokensResource;
import org.platformlayer.WellKnownPorts;
import org.platformlayer.auth.KeystoneJdbcModule;
import org.platformlayer.auth.keystone.KeystoneOpsUserModule;
import org.platformlayer.auth.server.GuiceAuthenticationConfig;
import org.platformlayer.cache.CacheModule;
import org.platformlayer.config.ConfigurationModule;
import org.platformlayer.metrics.MetricsSystem;
import org.platformlayer.metrics.client.codahale.CodahaleMetricsModule;
import org.platformlayer.web.CORSFilter;
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

public class KeystoneUserServer {
	private Server jettyServer;

	@Inject
	MetricsSystem metricsSystem;

	@Inject
	WebServerBuilder serverBuilder;

	@Inject
	Injector injector;

	public static void main(String[] args) throws Exception {
		List<Module> modules = Lists.newArrayList();
		modules.add(new ConfigurationModule());
		modules.add(new CacheModule());
		modules.add(new GuiceAuthenticationConfig());
		modules.add(new KeystoneJdbcModule());
		modules.add(new KeystoneOpsUserModule());
		modules.add(new CodahaleMetricsModule());
		modules.add(new JerseyServletModule() {
			@Override
			protected void configureServlets() {
				bind(CORSFilter.class).asEagerSingleton();
				filter("/*").through(CORSFilter.class);

				bind(TokensResource.class);
				bind(RegisterResource.class);

				Map<String, String> params = Maps.newHashMap();
				params.put(PackagesResourceConfig.PROPERTY_PACKAGES, "org.codehaus.jackson.jaxrs");
				serve("/*").with(GuiceContainer.class, params);
			}
		});

		Injector injector = Guice.createInjector(modules);

		KeystoneUserServer server = injector.getInstance(KeystoneUserServer.class);
		server.start(WellKnownPorts.PORT_PLATFORMLAYER_AUTH_USER);
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
