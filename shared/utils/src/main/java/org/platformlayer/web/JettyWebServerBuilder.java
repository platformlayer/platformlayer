package org.platformlayer.web;

import java.security.KeyStore;
import java.util.EnumSet;
import java.util.Set;

import javax.net.ssl.TrustManager;
import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.openstack.crypto.KeyStoreUtils;
import org.platformlayer.crypto.AcceptAllClientCertificatesTrustManager;
import org.platformlayer.crypto.EncryptionStore;
import org.platformlayer.ops.OpsException;

import com.fathomdb.crypto.CertificateAndKey;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;

public class JettyWebServerBuilder implements WebServerBuilder {
	@Inject(optional = true)
	EncryptionStore encryptionStore;

	Server server;

	public JettyWebServerBuilder() {
		server = new Server();
	}

	@Override
	public Server start() throws Exception {
		if (server.getThreadPool() == null) {
			server.setThreadPool(buildThreadPool());
		}

		server.start();
		return server;
	}

	protected ThreadPool buildThreadPool() {
		return new QueuedThreadPool();
	}

	@Override
	public void addHttpConnector(int port, boolean async) {
		Connector connector;
		if (async) {
			connector = buildSelectChannelConnector(port);
		} else {
			connector = buildSocketConnector(port);
		}

		// connector.setHost("127.0.0.1");
		server.addConnector(connector);
	}

	protected Connector buildSocketConnector(int port) {
		SocketConnector connector = new SocketConnector();
		connector.setPort(port);
		return connector;
	}

	protected Connector buildSelectChannelConnector(int port) {
		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setPort(port);
		return connector;
	}

	public ServletContextHandler addContext(String path) {
		ServletContextHandler context = new ServletContextHandler();
		context.setContextPath("/");

		server.setHandler(decorateContext(context));

		return context;
	}

	protected Handler decorateContext(ServletContextHandler context) {
		return context;
	}

	@Override
	public void addGuiceContext(String path, Injector injector) {
		ServletContextHandler context = addContext(path);

		GuiceServletConfig servletConfig = injector.getInstance(GuiceServletConfig.class);
		context.addEventListener(servletConfig);

		// Must add DefaultServlet for embedded Jetty
		// Failing to do this will cause 404 errors.
		context.addServlet(DefaultServlet.class, "/");

		FilterHolder filterHolder = new FilterHolder(GuiceFilter.class);
		context.addFilter(filterHolder, "*", EnumSet.of(DispatcherType.REQUEST));

		context.setClassLoader(Thread.currentThread().getContextClassLoader());
	}

	@Override
	public void addHttpsConnector(int port, Set<SslOption> options) throws Exception {
		SslContextFactory sslContextFactory;
		if (options.contains(SslOption.AllowAnyClientCertificate)) {
			CustomTrustManagerSslContextFactory customSslContextFactory = new CustomTrustManagerSslContextFactory();
			TrustManager[] trustManagers = new TrustManager[] { new AcceptAllClientCertificatesTrustManager() };
			customSslContextFactory.setTrustManagers(trustManagers);

			sslContextFactory = customSslContextFactory;
		} else {
			sslContextFactory = new SslContextFactory(SslContextFactory.DEFAULT_KEYSTORE_PATH);
		}

		{
			CertificateAndKey certificateAndKey = getCertificateAndKey();

			String secret = KeyStoreUtils.DEFAULT_KEYSTORE_SECRET;
			KeyStore keystore = KeyStoreUtils.createEmpty(secret);

			String alias = "https";

			KeyStoreUtils.put(keystore, alias, certificateAndKey, secret);

			sslContextFactory.setKeyStore(keystore);
			sslContextFactory.setKeyStorePassword(secret);
			sslContextFactory.setCertAlias(alias);
		}

		if (options.contains(SslOption.WantClientCertificate)) {
			sslContextFactory.setWantClientAuth(true);
		}

		SslSelectChannelConnector connector = new SslSelectChannelConnector(sslContextFactory);
		connector.setPort(port);
		server.addConnector(connector);
	}

	private CertificateAndKey getCertificateAndKey() throws OpsException {
		if (encryptionStore == null) {
			throw new IllegalStateException("EncryptionStore must be bound");
		}

		return encryptionStore.getCertificateAndKey("https");
	}

}
