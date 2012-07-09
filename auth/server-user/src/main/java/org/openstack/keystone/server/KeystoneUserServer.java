package org.openstack.keystone.server;

import java.io.File;
import java.security.KeyStore;
import java.util.EnumSet;

import javax.net.ssl.TrustManager;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.DispatcherType;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.openstack.crypto.KeyStoreUtils;
import org.platformlayer.WellKnownPorts;

import com.google.inject.servlet.GuiceFilter;

public class KeystoneUserServer {
	private Server server;

	public static void main(String[] args) throws Exception {
		KeystoneUserServer server = new KeystoneUserServer();

		File keystoreFile = new File("keystore.jks");
		String keystoreSecret = "notasecret";

		KeyStore keystore;

		try {
			keystore = KeyStoreUtils.load(keystoreFile, keystoreSecret);
		} catch (Exception e) {
			throw new IllegalArgumentException("Error loading SSL certificate from: " + keystoreFile.getAbsolutePath(),
					e);
		}

		server.start(WellKnownPorts.PORT_PLATFORMLAYER_AUTH_USER, keystore, keystoreSecret);
	}

	public void start(int port, KeyStore keystore, String keystorePassword) throws Exception {
		this.server = new Server();

		CustomTrustManagerSslContextFactory sslContextFactory = new CustomTrustManagerSslContextFactory();

		sslContextFactory.setKeyStore(keystore);
		sslContextFactory.setKeyStorePassword(keystorePassword);

		sslContextFactory.setWantClientAuth(true);

		TrustManager[] trustManagers = new TrustManager[] { new AcceptAllClientCertificatesTrustManager() };

		sslContextFactory.setTrustManagers(trustManagers);

		SslSelectChannelConnector connector = new SslSelectChannelConnector(sslContextFactory);
		connector.setPort(port);
		server.setConnectors(new Connector[] { connector });

		ServletContextHandler context = new ServletContextHandler();
		context.setContextPath("/");
		server.setHandler(context);

		context.addEventListener(new UserServerConfig());

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
