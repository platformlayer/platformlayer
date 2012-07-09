package org.platformlayer.xaas.web;

import java.security.KeyStore;
import java.util.EnumSet;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.DispatcherType;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openstack.crypto.CertificateAndKey;
import org.openstack.crypto.KeyStoreUtils;
import org.platformlayer.guice.JdbcGuiceModule;
import org.platformlayer.ops.crypto.EncryptionStore;
import org.platformlayer.ops.log.PerJobAppender;
import org.platformlayer.xaas.GuiceServletConfig;
import org.platformlayer.xaas.GuiceXaasConfig;
import org.platformlayer.xaas.PlatformLayerServletModule;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceFilter;

class StandaloneXaasWebserver {
	static final Logger log = Logger.getLogger(StandaloneXaasWebserver.class);

	static final int PORT = 8082;

	private Server server;

	@Inject
	EncryptionStore encryptionStore;

	@Inject
	Injector injector;

	public static void main(String[] args) throws Exception {
		List<Module> modules = Lists.newArrayList();
		// modules.add(new GuiceOpsConfig());
		modules.add(new GuiceXaasConfig());
		modules.add(new JdbcGuiceModule());
		modules.add(new PlatformLayerServletModule());

		Injector injector = Guice.createInjector(modules);

		StandaloneXaasWebserver server = injector.getInstance(StandaloneXaasWebserver.class);
		server.start();
	}

	public void start() throws Exception {
		PerJobAppender.attachToRootLogger();

		this.server = new Server();

		CertificateAndKey certificateAndKey = encryptionStore.getCertificateAndKey("https");

		{
			SslContextFactory sslContextFactory = new SslContextFactory(SslContextFactory.DEFAULT_KEYSTORE_PATH);

			String secret = KeyStoreUtils.DEFAULT_KEYSTORE_SECRET;
			KeyStore keystore = KeyStoreUtils.createEmpty(secret);

			String alias = "https";

			KeyStoreUtils.put(keystore, alias, certificateAndKey, secret);

			sslContextFactory.setKeyStore(keystore);
			sslContextFactory.setKeyStorePassword(secret);
			sslContextFactory.setCertAlias(alias);

			SslSelectChannelConnector connector = new SslSelectChannelConnector(sslContextFactory);
			connector.setPort(PORT);
			server.setConnectors(new Connector[] { connector });
		}

		ServletContextHandler context = new ServletContextHandler();
		context.setContextPath("/");
		server.setHandler(context);

		context.addEventListener(new GuiceServletConfig(injector));

		// Must add DefaultServlet for embedded Jetty
		// Failing to do this will cause 404 errors.
		context.addServlet(DefaultServlet.class, "/");

		FilterHolder filterHolder = new FilterHolder(GuiceFilter.class);
		context.addFilter(filterHolder, "*", EnumSet.of(DispatcherType.REQUEST));

		context.setClassLoader(Thread.currentThread().getContextClassLoader());

		server.start();
	}

	public void stop() throws Exception {
		if (server != null) {
			server.stop();
		}
	}

}
