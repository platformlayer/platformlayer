package org.platformlayer.xaas.web;

import java.io.File;
import java.security.KeyStore;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import javax.inject.Inject;
import javax.net.ssl.SSLContext;
import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;
import org.platformlayer.cache.CacheModule;
import org.platformlayer.config.ConfigurationModule;
import org.platformlayer.guice.xaas.ItemEntity;
import org.platformlayer.guice.xaas.TagEntity;
import org.platformlayer.jdbc.JdbcGuiceModule;
import org.platformlayer.jdbc.simplejpa.ResultSetMappersProvider;
import org.platformlayer.metrics.NullMetricsModule;
import org.platformlayer.ops.extensions.Extensions;
import org.platformlayer.ops.jobrunner.JobPoller;
import org.platformlayer.ops.jobstore.jdbc.JobEntity;
import org.platformlayer.ops.jobstore.jdbc.JobExecutionEntity;
import org.platformlayer.ops.log.LogbackHook;
import org.platformlayer.ops.schedule.Scheduler;
import org.platformlayer.ops.schedule.jdbc.SchedulerRecordEntity;
import org.platformlayer.web.GuiceServletConfig;
import org.platformlayer.xaas.GuiceXaasConfig;
import org.platformlayer.xaas.PlatformLayerServletModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.Configuration;
import com.fathomdb.config.ConfigurationImpl;
import com.fathomdb.crypto.CertificateAndKey;
import com.fathomdb.crypto.EncryptionStore;
import com.fathomdb.crypto.KeyStoreUtils;
import com.fathomdb.crypto.ssl.SslPolicy;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceFilter;

class StandaloneXaasWebserver {
	static final Logger log = LoggerFactory.getLogger(StandaloneXaasWebserver.class);

	static final int PORT = 8082;

	private Server server;

	@Inject
	GuiceServletConfig guiceServletConfig;

	@Inject
	EncryptionStore encryptionStore;

	@Inject
	Scheduler scheduler;

	@Inject
	JobPoller jobPoller;

	@Inject
	Configuration configuration;

	final Map<String, File> wars = Maps.newHashMap();

	public static void main(String[] args) {
		try {
			// Force GMT
			TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

			ConfigurationImpl configuration = ConfigurationImpl.load();

			Extensions extensions = Extensions.load(configuration);

			List<Module> modules = Lists.newArrayList();
			modules.add(new NullMetricsModule());
			modules.add(new GuiceXaasConfig(configuration));
			modules.add(new ConfigurationModule(configuration));
			modules.add(new CacheModule());
			modules.add(new JdbcGuiceModule());
			modules.add(new PlatformLayerServletModule(extensions));
			modules.add(new PlatformlayerValidationModule());

			Injector injector = extensions.createInjector(modules);

			ResultSetMappersProvider provider = injector.getInstance(ResultSetMappersProvider.class);
			provider.addAll(ItemEntity.class, TagEntity.class, SchedulerRecordEntity.class, JobEntity.class,
					JobExecutionEntity.class);

			extensions.addEntities(provider);

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
		LogbackHook.attachToRootLogger();

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

			// TODO: Preconfigure a better SSLContext??
			SSLContext sslContext = SSLContext.getDefault();
			sslContextFactory.setIncludeCipherSuites(SslPolicy.DEFAULT.getEngineConfig(sslContext)
					.getEnabledCipherSuites());
			sslContextFactory.setIncludeProtocols(SslPolicy.DEFAULT.getEngineConfig(sslContext).getEnabledProtocols());

			SslSelectChannelConnector connector = new SslSelectChannelConnector(sslContextFactory);
			connector.setPort(PORT);
			String host = configuration.lookup("http.host", null);
			if (host != null) {
				connector.setHost(host);
			}

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

			context.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");

			context.addFilter(GwtCacheHeaderFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));

			contexts.addHandler(context);
		}

		server.setHandler(contexts);

		server.addLifeCycleListener(new CloseOnFailLifecycleListener());

		server.start();

		if (!server.isStarted()) {
			return false;
		}

		if (configuration.lookup("jobrunner.enabled", true)) {
			scheduler.start();

			jobPoller.start();
		}

		return true;
	}

	public void stop() throws Exception {
		if (server != null) {
			server.stop();
		}
	}

}
