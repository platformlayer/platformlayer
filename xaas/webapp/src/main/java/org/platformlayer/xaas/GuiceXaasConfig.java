package org.platformlayer.xaas;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import org.platformlayer.PlatformLayerClient;
import org.platformlayer.auth.AuthenticationService;
import org.platformlayer.auth.AuthenticationTokenValidator;
import org.platformlayer.auth.client.PlatformLayerAuthAdminClient;
import org.platformlayer.auth.client.PlatformlayerAuthenticationClient;
import org.platformlayer.auth.client.PlatformlayerAuthenticationService;
import org.platformlayer.crypto.EncryptionStore;
import org.platformlayer.crypto.KeyStoreEncryptionStore;
import org.platformlayer.guice.xaas.ItemEntity;
import org.platformlayer.guice.xaas.JdbcJobRepository;
import org.platformlayer.guice.xaas.JdbcManagedItemRepository;
import org.platformlayer.guice.xaas.JdbcServiceAuthorizationRepository;
import org.platformlayer.guice.xaas.TagEntity;
import org.platformlayer.http.HttpStrategy;
import org.platformlayer.http.apache.InstrumentedApacheHttpStrategy;
import org.platformlayer.inject.GuiceObjectInjector;
import org.platformlayer.inject.ObjectInjector;
import org.platformlayer.jdbc.GuiceDataSourceProvider;
import org.platformlayer.jdbc.simplejpa.ResultSetMappers;
import org.platformlayer.jdbc.simplejpa.ResultSetMappersProvider;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.crypto.OpsKeyStore;
import org.platformlayer.ops.crypto.SimpleOpsKeyStore;
import org.platformlayer.ops.guice.OpsContextProvider;
import org.platformlayer.ops.schedule.jdbc.SchedulerRecordEntity;
import org.platformlayer.ops.ssh.ISshContext;
import org.platformlayer.ops.tasks.OperationQueue;
import org.platformlayer.ops.tasks.SimpleOperationQueue;
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

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class GuiceXaasConfig extends AbstractModule {

	@Override
	protected void configure() {
		bind(EncryptionStore.class).toProvider(KeyStoreEncryptionStore.Provider.class);

		bind(ISshContext.class).to(MinaSshContext.class);

		bind(OpsSystem.class);

		bind(OpsContext.class).toProvider(OpsContextProvider.class);

		// TODO: Split off scheduler
		bind(ResultSetMappers.class).toProvider(
				ResultSetMappersProvider.build(ItemEntity.class, TagEntity.class, SchedulerRecordEntity.class));

		bind(DataSource.class).toProvider(GuiceDataSourceProvider.bind("platformlayer.jdbc.")).asEagerSingleton();

		JerseyAnnotationDiscovery discovery = new JerseyAnnotationDiscovery();
		discovery.scan();
		bind(AnnotationDiscovery.class).toInstance(discovery);

		HttpStrategy httpStrategy = new InstrumentedApacheHttpStrategy();
		bind(HttpStrategy.class).toInstance(httpStrategy);

		bind(AuthenticationTokenValidator.class).toProvider(PlatformLayerAuthAdminClient.Provider.class).in(
				Scopes.SINGLETON);

		// boolean isMultitenant = !Strings.isNullOrEmpty(configuration.lookup("multitenant.keys", null));
		if (true) { // isMultitenant) {
			bind(PlatformlayerAuthenticationClient.class).toProvider(PlatformlayerAuthenticationClient.Provider.class)
					.asEagerSingleton();

			bind(AuthenticationService.class).to(PlatformlayerAuthenticationService.class).asEagerSingleton();
		}

		bind(ExecutorService.class).toInstance(Executors.newCachedThreadPool());

		bind(ServiceProviderDictionary.class).to(AnnotationServiceProviderDictionary.class).in(Scopes.SINGLETON);

		bind(JaxbContextHelper.class).asEagerSingleton();

		bind(OperationQueue.class).to(SimpleOperationQueue.class).asEagerSingleton();

		bind(ObjectInjector.class).to(GuiceObjectInjector.class);

		bind(OpsKeyStore.class).to(SimpleOpsKeyStore.class).in(Scopes.SINGLETON);

		bind(JobRepository.class).to(JdbcJobRepository.class);
		bind(ManagedItemRepository.class).to(JdbcManagedItemRepository.class);
		bind(ServiceAuthorizationRepository.class).to(JdbcServiceAuthorizationRepository.class);

		bind(ChangeQueue.class).to(InProcessChangeQueue.class);

		bind(PlatformLayerClient.class).toProvider(PlatformLayerClientProvider.class);
	}

}
