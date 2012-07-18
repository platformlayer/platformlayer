package org.openstack.keystone.server;

import java.security.KeyStore;
import java.util.EnumSet;
import java.util.Map;

import javax.inject.Inject;
import javax.net.ssl.TrustManager;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.DispatcherType;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.openstack.crypto.CertificateAndKey;
import org.openstack.crypto.KeyStoreUtils;
import org.openstack.keystone.resources.user.TokensResource;
import org.platformlayer.WellKnownPorts;
import org.platformlayer.auth.server.AcceptAllClientCertificatesTrustManager;
import org.platformlayer.auth.server.CustomTrustManagerSslContextFactory;
import org.platformlayer.auth.server.GuiceAuthenticationConfig;
import org.platformlayer.auth.server.GuiceServletConfig;
import org.platformlayer.crypto.EncryptionStore;

import com.google.common.collect.Maps;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

public class KeystoneUserServer {
	private Server server;

	@Inject
	GuiceServletConfig servletConfig;

	@Inject
	EncryptionStore encryptionStore;

	public static void main(String[] args) throws Exception {
		Injector injector = Guice.createInjector(new GuiceAuthenticationConfig(), new JerseyServletModule() {
			@Override
			protected void configureServlets() {
				bind(TokensResource.class);

				Map<String, String> params = Maps.newHashMap();
				params.put(PackagesResourceConfig.PROPERTY_PACKAGES, "org.codehaus.jackson.jaxrs");
				serve("/*").with(GuiceContainer.class, params);
			}
		});

		KeystoneUserServer server = injector.getInstance(KeystoneUserServer.class);
		server.start(WellKnownPorts.PORT_PLATFORMLAYER_AUTH_USER);
	}

	public void start(int port) throws Exception {
		this.server = new Server();

		{
			CustomTrustManagerSslContextFactory sslContextFactory = new CustomTrustManagerSslContextFactory();

			{
				CertificateAndKey certificateAndKey = encryptionStore.getCertificateAndKey("https");

				String secret = KeyStoreUtils.DEFAULT_KEYSTORE_SECRET;
				KeyStore keystore = KeyStoreUtils.createEmpty(secret);

				String alias = "https";

				KeyStoreUtils.put(keystore, alias, certificateAndKey, secret);

				sslContextFactory.setKeyStore(keystore);
				sslContextFactory.setKeyStorePassword(secret);
				sslContextFactory.setCertAlias(alias);
			}
			sslContextFactory.setWantClientAuth(true);

			TrustManager[] trustManagers = new TrustManager[] { new AcceptAllClientCertificatesTrustManager() };

			sslContextFactory.setTrustManagers(trustManagers);

			SslSelectChannelConnector connector = new SslSelectChannelConnector(sslContextFactory);
			connector.setPort(port);
			server.setConnectors(new Connector[] { connector });
		}

		ServletContextHandler context = new ServletContextHandler();
		context.setContextPath("/");
		server.setHandler(context);

		context.addEventListener(servletConfig);

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
