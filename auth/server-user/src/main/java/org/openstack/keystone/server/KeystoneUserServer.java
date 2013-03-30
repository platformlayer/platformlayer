package org.openstack.keystone.server;

import java.io.File;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.jetty.server.Server;
import org.openstack.keystone.resources.user.UserAuthServletModule;
import org.platformlayer.WellKnownPorts;
import org.platformlayer.auth.KeystoneJdbcModule;
import org.platformlayer.auth.keystone.KeystoneOpsUserModule;
import org.platformlayer.auth.server.GuiceAuthenticationConfig;
import org.platformlayer.auth.services.LoginLimits;
import org.platformlayer.auth.services.LoginService;
import org.platformlayer.cache.CacheModule;
import org.platformlayer.config.ConfigurationModule;
import org.platformlayer.extensions.Extensions;
import org.platformlayer.metrics.MetricReporter;
import org.platformlayer.metrics.client.codahale.CodahaleMetricsModule;
import org.platformlayer.web.SslOption;
import org.platformlayer.web.WebServerBuilder;

import com.fathomdb.Configuration;
import com.fathomdb.config.ConfigurationImpl;
import com.fathomdb.discovery.Discovery;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public class KeystoneUserServer {
	private Server jettyServer;

	@Inject
	MetricReporter metricReporter;

	@Inject
	WebServerBuilder serverBuilder;

	@Inject
	Injector injector;

	@Inject
	Configuration configuration;

	@Inject
	LoginService loginService;

	@Inject
	LoginLimits loginLimits;

	public static void main(String[] args) throws Exception {
		List<Module> modules = Lists.newArrayList();

		ConfigurationModule configurationModule = new ConfigurationModule();
		modules.add(configurationModule);

		Discovery discovery = Discovery.build();

		ConfigurationImpl configuration = configurationModule.getConfiguration();
		Extensions extensions = new Extensions(configuration, discovery);

		modules.add(new CacheModule());
		modules.add(new GuiceAuthenticationConfig());
		modules.add(new KeystoneJdbcModule());
		modules.add(new KeystoneOpsUserModule());
		modules.add(new CodahaleMetricsModule());
		modules.add(new UserAuthServletModule(extensions));

		Injector injector = Guice.createInjector(modules);

		KeystoneUserServer server = injector.getInstance(KeystoneUserServer.class);
		server.start(WellKnownPorts.PORT_PLATFORMLAYER_AUTH_USER);
	}

	public void start(int port) throws Exception {
		EnumSet<SslOption> options = EnumSet.of(SslOption.AllowAnyClientCertificate, SslOption.WantClientCertificate);

		serverBuilder.addHttpsConnector(port, options);
		serverBuilder.addGuiceContext("/", injector);

		Map<String, String> wars = configuration.getChildProperties("war.");
		for (Map.Entry<String, String> war : wars.entrySet()) {
			serverBuilder.addWar(war.getKey(), new File(war.getValue()));
		}

		this.jettyServer = serverBuilder.start();

		metricReporter.start();
	}

	public void stop() throws Exception {
		if (jettyServer != null) {
			jettyServer.stop();
		}
	}
}
