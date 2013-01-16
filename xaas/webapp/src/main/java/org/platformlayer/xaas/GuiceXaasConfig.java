package org.platformlayer.xaas;

import java.io.File;
import java.net.URLClassLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;

import org.platformlayer.PlatformLayerClient;
import org.platformlayer.auth.AuthenticationService;
import org.platformlayer.auth.AuthenticationTokenValidator;
import org.platformlayer.auth.client.PlatformLayerAuthenticationClient;
import org.platformlayer.auth.client.PlatformLayerAuthenticationClientProvider;
import org.platformlayer.auth.system.PlatformLayerAuthAdminClient;
import org.platformlayer.auth.system.PlatformlayerAuthenticationService;
import org.platformlayer.config.ConfigurationImpl;
import org.platformlayer.crypto.EncryptionStore;
import org.platformlayer.crypto.KeyStoreEncryptionStore;
import org.platformlayer.guice.xaas.JdbcManagedItemRepository;
import org.platformlayer.guice.xaas.JdbcServiceAuthorizationRepository;
import org.platformlayer.http.HttpStrategy;
import org.platformlayer.http.apache.InstrumentedApacheHttpStrategy;
import org.platformlayer.inject.GuiceObjectInjector;
import org.platformlayer.inject.ObjectInjector;
import org.platformlayer.jdbc.GuiceDataSourceProvider;
import org.platformlayer.jdbc.simplejpa.ResultSetMappers;
import org.platformlayer.jdbc.simplejpa.ResultSetMappersProvider;
import org.platformlayer.ops.ItemService;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.backups.BackupContextFactory;
import org.platformlayer.ops.backups.StubBackupContextFactory;
import org.platformlayer.ops.crypto.OpsKeyStore;
import org.platformlayer.ops.crypto.SimpleOpsKeyStore;
import org.platformlayer.ops.guice.OpsContextProvider;
import org.platformlayer.ops.jobstore.FilesystemJobLogStore;
import org.platformlayer.ops.jobstore.JobLogStore;
import org.platformlayer.ops.jobstore.PersistentJobRegistry;
import org.platformlayer.ops.jobstore.SimpleOperationQueue;
import org.platformlayer.ops.jobstore.jdbc.JdbcJobRepository;
import org.platformlayer.ops.ssh.ISshContext;
import org.platformlayer.ops.tasks.JobRegistry;
import org.platformlayer.ops.tasks.OperationQueue;
import org.platformlayer.ops.templates.FreemarkerTemplateEngine;
import org.platformlayer.ops.templates.TemplateEngine;
import org.platformlayer.ssh.mina.MinaSshContext;
import org.platformlayer.xaas.discovery.AnnotationDiscovery;
import org.platformlayer.xaas.discovery.JerseyAnnotationDiscovery;
import org.platformlayer.xaas.ops.InProcessChangeQueue;
import org.platformlayer.xaas.repository.JobRepository;
import org.platformlayer.xaas.repository.ManagedItemRepository;
import org.platformlayer.xaas.repository.ServiceAuthorizationRepository;
import org.platformlayer.xaas.services.AnnotationServiceProviderDictionary;
import org.platformlayer.xaas.services.ChangeQueue;
import org.platformlayer.xaas.services.ServiceProviderDictionary;
import org.platformlayer.xaas.web.jaxrs.JaxbContextHelper;
import org.platformlayer.xaas.web.resources.ItemServiceImpl;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class GuiceXaasConfig extends AbstractModule {

	final ConfigurationImpl configuration;

	public GuiceXaasConfig(ConfigurationImpl configuration) {
		super();
		this.configuration = configuration;
	}

	@Override
	protected void configure() {
		bind(EncryptionStore.class).toProvider(KeyStoreEncryptionStore.Provider.class);

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		FreemarkerTemplateEngine freemarker = new FreemarkerTemplateEngine(classLoader);
		bind(TemplateEngine.class).toInstance(freemarker);

		bind(ItemService.class).to(ItemServiceImpl.class);

		bind(ISshContext.class).to(MinaSshContext.class);

		bind(OpsSystem.class);

		bind(OpsContext.class).toProvider(OpsContextProvider.class);

		bind(JobRegistry.class).to(PersistentJobRegistry.class).asEagerSingleton();

		File jobLogStoreBaseDir = new File("jobs");
		jobLogStoreBaseDir.mkdirs();
		bind(JobLogStore.class).toInstance(new FilesystemJobLogStore(jobLogStoreBaseDir));

		// TODO: Split off scheduler
		bind(ResultSetMappersProvider.class).asEagerSingleton();
		bind(ResultSetMappers.class).toProvider(ResultSetMappersProvider.class).in(Scopes.SINGLETON);

		bind(DataSource.class).toProvider(GuiceDataSourceProvider.bind("platformlayer.jdbc.")).asEagerSingleton();

		URLClassLoader urlClassLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
		JerseyAnnotationDiscovery discovery = new JerseyAnnotationDiscovery();
		discovery.scan(urlClassLoader);
		bind(AnnotationDiscovery.class).toInstance(discovery);

		HttpStrategy httpStrategy = new InstrumentedApacheHttpStrategy();
		bind(HttpStrategy.class).toInstance(httpStrategy);

		bind(AuthenticationTokenValidator.class).toProvider(PlatformLayerAuthAdminClient.Provider.class).in(
				Scopes.SINGLETON);

		// boolean isMultitenant = !Strings.isNullOrEmpty(configuration.lookup("multitenant.keys", null));
		if (true) { // isMultitenant) {
			bind(PlatformLayerAuthenticationClient.class).toProvider(PlatformLayerAuthenticationClientProvider.class)
					.asEagerSingleton();

			bind(AuthenticationService.class).to(PlatformlayerAuthenticationService.class).asEagerSingleton();
		}

		bind(BackupContextFactory.class).to(StubBackupContextFactory.class);

		bind(ExecutorService.class).toInstance(Executors.newCachedThreadPool());

		bind(ServiceProviderDictionary.class).to(AnnotationServiceProviderDictionary.class).in(Scopes.SINGLETON);

		bind(JaxbContextHelper.class).asEagerSingleton();
		bind(JAXBContext.class).toProvider(JaxbContextHelper.class);

		if (!configuration.isExplicitlyBound(OperationQueue.class)) {
			bind(OperationQueue.class).to(SimpleOperationQueue.class).asEagerSingleton();
		}

		bind(ObjectInjector.class).to(GuiceObjectInjector.class);

		bind(OpsKeyStore.class).to(SimpleOpsKeyStore.class).in(Scopes.SINGLETON);

		bind(JobRepository.class).to(JdbcJobRepository.class);
		bind(ManagedItemRepository.class).to(JdbcManagedItemRepository.class);
		bind(ServiceAuthorizationRepository.class).to(JdbcServiceAuthorizationRepository.class);

		bind(ChangeQueue.class).to(InProcessChangeQueue.class);

		bind(PlatformLayerClient.class).toProvider(PlatformLayerClientProvider.class);
	}

}
