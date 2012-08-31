package org.platformlayer.xaas.web;

import java.io.File;
import java.security.KeyStore;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.servlet.DispatcherType;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;
import org.openstack.crypto.CertificateAndKey;
import org.openstack.crypto.KeyStoreUtils;
import org.platformlayer.cache.CacheModule;
import org.platformlayer.config.ConfigurationModule;
import org.platformlayer.crypto.EncryptionStore;
import org.platformlayer.jdbc.JdbcGuiceModule;
import org.platformlayer.metrics.NullMetricsModule;
import org.platformlayer.ops.log.PerJobAppender;
import org.platformlayer.ops.schedule.Scheduler;
import org.platformlayer.web.GuiceServletConfig;
import org.platformlayer.xaas.GuiceXaasConfig;
import org.platformlayer.xaas.PlatformLayerServletModule;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceFilter;

class StandaloneXaasWebserver {
	static final Logger log = Logger.getLogger(StandaloneXaasWebserver.class);

	static final int PORT = 8082;

	private Server server;

	@Inject
	GuiceServletConfig guiceServletConfig;

	@Inject
	EncryptionStore encryptionStore;

	@Inject
	Scheduler scheduler;

	final Map<String, File> wars = Maps.newHashMap();

	public static void main(String[] args) {
		try {
			List<Module> modules = Lists.newArrayList();
			// modules.add(new GuiceOpsConfig());
			modules.add(new NullMetricsModule());
			modules.add(new GuiceXaasConfig());
			modules.add(new ConfigurationModule());
			modules.add(new CacheModule());
			modules.add(new JdbcGuiceModule());
			modules.add(new PlatformLayerServletModule());

			Injector injector = Guice.createInjector(modules);

			StandaloneXaasWebserver server = injector.getInstance(StandaloneXaasWebserver.class);

			// Temporary hack
			if (args.length != 0) {
				log.warn("Insert WAR onto root: " + args[0]);
				File rootWar = new File(args[0]);
				server.wars.put("/", rootWar);
			}

			if (!server.start()) {
				log.error("Failed to start webserver");
				System.exit(1);
			}
		} catch (Throwable e) {
			log.error("Error in initialization", e);
			System.exit(1);
		}

	}

	public boolean start() throws Exception {
		PerJobAppender.attachToRootLogger();

		this.server = new Server();

		{
			SslContextFactory sslContextFactory = new SslContextFactory(SslContextFactory.DEFAULT_KEYSTORE_PATH);

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

			SslSelectChannelConnector connector = new SslSelectChannelConnector(sslContextFactory);
			connector.setPort(PORT);
			server.setConnectors(new Connector[] { connector });
		}

		ContextHandlerCollection contexts = new ContextHandlerCollection();

		{
			ServletContextHandler context = new ServletContextHandler(contexts, "/api");
			// context.setContextPath("/");
			context.addEventListener(guiceServletConfig);

			// Must add DefaultServlet for embedded Jetty
			// Failing to do this will cause 404 errors.
			context.addServlet(DefaultServlet.class, "/");

			FilterHolder filterHolder = new FilterHolder(GuiceFilter.class);
			context.addFilter(filterHolder, "*", EnumSet.of(DispatcherType.REQUEST));

			context.setClassLoader(Thread.currentThread().getContextClassLoader());
		}

		for (Entry<String, File> entry : wars.entrySet()) {
			String contextPath = entry.getKey();
			File war = entry.getValue();

			WebAppContext context = new WebAppContext();
			context.setWar(war.getAbsolutePath());
			context.setContextPath(contextPath);
			contexts.addHandler(context);
		}

		server.setHandler(contexts);

		server.addLifeCycleListener(new CloseOnFailLifecycleListener());

		server.start();

		if (!server.isStarted()) {
			return false;
		}

		scheduler.start();
		return true;
	}

	public void stop() throws Exception {
		if (server != null) {
			server.stop();
		}
	}

}
