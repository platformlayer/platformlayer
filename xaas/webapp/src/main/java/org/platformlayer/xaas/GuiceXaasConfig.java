package org.platformlayer.xaas;

import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
import javax.sql.DataSource;

import org.platformlayer.PlatformLayerClient;
import org.platformlayer.WellKnownPorts;
import org.platformlayer.auth.AuthenticationService;
import org.platformlayer.auth.AuthenticationTokenValidator;
import org.platformlayer.auth.client.PlatformLayerAuthAdminClient;
import org.platformlayer.auth.client.PlatformlayerAuthenticationClient;
import org.platformlayer.auth.client.PlatformlayerAuthenticationService;
import org.platformlayer.config.Configuration;
import org.platformlayer.crypto.AcceptAllHostnameVerifier;
import org.platformlayer.crypto.EncryptionStore;
import org.platformlayer.crypto.KeyStoreEncryptionStore;
import org.platformlayer.crypto.PublicKeyTrustManager;
import org.platformlayer.guice.GuiceDataSourceProvider;
import org.platformlayer.guice.xaas.ItemEntity;
import org.platformlayer.guice.xaas.JdbcJobRepository;
import org.platformlayer.guice.xaas.JdbcManagedItemRepository;
import org.platformlayer.guice.xaas.JdbcServiceAuthorizationRepository;
import org.platformlayer.guice.xaas.TagEntity;
import org.platformlayer.http.HttpStrategy;
import org.platformlayer.http.SslConfiguration;
import org.platformlayer.http.apache.InstrumentedApacheHttpStrategy;
import org.platformlayer.inject.GuiceObjectInjector;
import org.platformlayer.inject.ObjectInjector;
import org.platformlayer.jdbc.simplejpa.ResultSetMappers;
import org.platformlayer.jdbc.simplejpa.ResultSetMappersProvider;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.crypto.OpsKeyStore;
import org.platformlayer.ops.crypto.SimpleOpsKeyStore;
import org.platformlayer.ops.guice.OpsContextProvider;
import org.platformlayer.ops.schedule.jdbc.SchedulerRecordEntity;
import org.platformlayer.ops.ssh.ISshContext;
import org.platformlayer.ops.tasks.OperationQueue;
import org.platformlayer.ops.tasks.SimpleOperationQueue;
import org.platformlayer.rest.JreRestfulClient;
import org.platformlayer.rest.RestfulClient;
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

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class GuiceXaasConfig extends AbstractModule {

	@Override
	protected void configure() {
		try {
			Configuration configuration = Configuration.load();
			bind(Configuration.class).toInstance(configuration);

			configuration.bindProperties(binder());

			EncryptionStore encryptionStore = KeyStoreEncryptionStore.build(configuration);
			bind(EncryptionStore.class).toInstance(encryptionStore);

			bind(ISshContext.class).to(MinaSshContext.class);

			bind(OpsSystem.class);

			try {
				doExplicitBindings(configuration);
			} catch (ClassNotFoundException e) {
				throw new OpsException("Class not found during binding", e);
			}

			bind(OpsContext.class).toProvider(OpsContextProvider.class);

			// TODO: Split off scheduler
			bind(ResultSetMappers.class).toProvider(
					ResultSetMappersProvider.build(ItemEntity.class, TagEntity.class, SchedulerRecordEntity.class));

			bind(DataSource.class).toProvider(
					GuiceDataSourceProvider.fromConfiguration(configuration, "platformlayer.jdbc."));

			JerseyAnnotationDiscovery discovery = new JerseyAnnotationDiscovery();
			discovery.scan();
			bind(AnnotationDiscovery.class).toInstance(discovery);

			HttpStrategy httpStrategy = new InstrumentedApacheHttpStrategy();
			bind(HttpStrategy.class).toInstance(httpStrategy);

			PlatformLayerAuthAdminClient tokenValidator = PlatformLayerAuthAdminClient.build(httpStrategy,
					configuration, encryptionStore);
			bind(AuthenticationTokenValidator.class).toInstance(tokenValidator);

			boolean isMultitenant = !Strings.isNullOrEmpty(configuration.lookup("multitenant.keys", null));
			if (true) { // isMultitenant) {
				bindUserAuth(httpStrategy, encryptionStore, configuration);

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
		} catch (OpsException e) {
			throw new IllegalStateException("Error during configuration", e);
		}
	}

	private void doExplicitBindings(Configuration configuration) throws ClassNotFoundException {
		Properties bindings = configuration.getChildProperties("bind.");
		for (Entry<Object, Object> entry : bindings.entrySet()) {
			String serviceKey = (String) entry.getKey();
			Class service = Class.forName(serviceKey);
			Class implementation = Class.forName((String) entry.getValue());

			bind(service).to(implementation);
		}
	}

	private void bindUserAuth(HttpStrategy httpStrategy, EncryptionStore encryptionStore, Configuration configuration)
			throws OpsException {
		String keystoneUserUrl = configuration.lookup("auth.user.url", "https://127.0.0.1:"
				+ WellKnownPorts.PORT_PLATFORMLAYER_AUTH_USER + "/v2.0/");

		HostnameVerifier hostnameVerifier = null;

		KeyManager keyManager = null;

		TrustManager trustManager = null;

		String trustKeys = configuration.lookup("auth.user.ssl.keys", null);

		if (trustKeys != null) {
			trustManager = new PublicKeyTrustManager(Splitter.on(',').trimResults().split(trustKeys));

			hostnameVerifier = new AcceptAllHostnameVerifier();
		}

		SslConfiguration sslConfiguration = new SslConfiguration(keyManager, trustManager, hostnameVerifier);
		RestfulClient restfulClient = new JreRestfulClient(httpStrategy, keystoneUserUrl, sslConfiguration);
		PlatformlayerAuthenticationClient authClient = new PlatformlayerAuthenticationClient(restfulClient);

		bind(PlatformlayerAuthenticationClient.class).toInstance(authClient);
	}

}
