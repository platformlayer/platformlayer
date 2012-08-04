package org.platformlayer.xaas;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
import javax.sql.DataSource;

import org.openstack.crypto.CertificateAndKey;
import org.openstack.crypto.KeyStoreUtils;
import org.openstack.utils.PropertyUtils;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.WellKnownPorts;
import org.platformlayer.auth.AuthenticationService;
import org.platformlayer.auth.client.PlatformLayerTokenValidator;
import org.platformlayer.auth.client.PlatformlayerAuthenticationClient;
import org.platformlayer.auth.client.PlatformlayerAuthenticationService;
import org.platformlayer.crypto.AcceptAllHostnameVerifier;
import org.platformlayer.crypto.EncryptionStore;
import org.platformlayer.crypto.KeyStoreEncryptionStore;
import org.platformlayer.crypto.PublicKeyTrustManager;
import org.platformlayer.crypto.SimpleClientCertificateKeyManager;
import org.platformlayer.guice.GuiceDataSourceProvider;
import org.platformlayer.guice.GuiceObjectInjector;
import org.platformlayer.guice.xaas.ItemEntity;
import org.platformlayer.guice.xaas.JdbcJobRepository;
import org.platformlayer.guice.xaas.JdbcManagedItemRepository;
import org.platformlayer.guice.xaas.JdbcServiceAuthorizationRepository;
import org.platformlayer.guice.xaas.TagEntity;
import org.platformlayer.inject.ObjectInjector;
import org.platformlayer.jdbc.simplejpa.ResultSetMappers;
import org.platformlayer.jdbc.simplejpa.ResultSetMappersProvider;
import org.platformlayer.ops.OpsConfiguration;
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
		String configFilePath = System.getProperty("conf");
		if (configFilePath == null) {
			configFilePath = new File(new File("."), "configuration.properties").getAbsolutePath();
		}

		File configFile = new File(configFilePath);

		Properties applicationProperties;
		try {
			applicationProperties = PropertyUtils.loadProperties(configFile);
		} catch (IOException e) {
			throw new IllegalStateException("Error loading configuration file: " + configFile, e);
		}

		try {
			OpsConfiguration configuration = new OpsConfiguration(applicationProperties);
			bind(OpsConfiguration.class).toInstance(configuration);

			configuration.bindProperties(binder());

			EncryptionStore encryptionStore = bindEncryptionStore(configuration);

			bind(ISshContext.class).to(MinaSshContext.class);

			bind(OpsSystem.class);

			bind(OpsContext.class).toProvider(OpsContextProvider.class);

			// TODO: Split off scheduler
			bind(ResultSetMappers.class).toProvider(
					ResultSetMappersProvider.build(ItemEntity.class, TagEntity.class, SchedulerRecordEntity.class));

			bind(DataSource.class).toProvider(GuiceDataSourceProvider.fromProperties(null, "platformlayer.jdbc."));

			JerseyAnnotationDiscovery discovery = new JerseyAnnotationDiscovery();
			discovery.scan();
			bind(AnnotationDiscovery.class).toInstance(discovery);

			bindAuthTokenValidator(encryptionStore, configuration);

			boolean isMultitenant = !Strings.isNullOrEmpty(configuration.lookup("multitenant.keys", null));
			if (true) { // isMultitenant) {
				bindUserAuth(encryptionStore, configuration);

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
			throw new IllegalStateException("Cannot during configuration", e);
		}
	}

	private void bindAuthTokenValidator(EncryptionStore encryptionStore, OpsConfiguration configuration)
			throws OpsException {
		String keystoneServiceUrl = configuration.lookup("auth.system.url", "https://127.0.0.1:"
				+ WellKnownPorts.PORT_PLATFORMLAYER_AUTH_ADMIN + "/");

		String cert = configuration.get("auth.system.ssl.cert");
		// String secret = configuration.lookup("multitenant.cert.password", KeyStoreUtils.DEFAULT_KEYSTORE_SECRET);

		CertificateAndKey certificateAndKey = encryptionStore.getCertificateAndKey(cert);

		HostnameVerifier hostnameVerifier = null;

		KeyManager keyManager = new SimpleClientCertificateKeyManager(certificateAndKey);

		TrustManager trustManager = null;

		String trustKeys = configuration.lookup("auth.system.ssl.keys", null);

		if (trustKeys != null) {
			trustManager = new PublicKeyTrustManager(Splitter.on(',').trimResults().split(trustKeys));

			hostnameVerifier = new AcceptAllHostnameVerifier();
		}

		PlatformLayerTokenValidator keystoneTokenValidator = new PlatformLayerTokenValidator(keystoneServiceUrl,
				keyManager, trustManager, hostnameVerifier);

		bind(PlatformLayerTokenValidator.class).toInstance(keystoneTokenValidator);
	}

	private void bindUserAuth(EncryptionStore encryptionStore, OpsConfiguration configuration) throws OpsException {
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

		PlatformlayerAuthenticationClient authClient = new PlatformlayerAuthenticationClient(keystoneUserUrl,
				keyManager, trustManager, hostnameVerifier);

		bind(PlatformlayerAuthenticationClient.class).toInstance(authClient);
	}

	private EncryptionStore bindEncryptionStore(OpsConfiguration configuration) {
		String keystorePath = configuration.lookup("keystore", "keystore.jks");
		String secret = configuration.lookup("keystore.password", KeyStoreUtils.DEFAULT_KEYSTORE_SECRET);

		File keystoreFile = new File(keystorePath);

		EncryptionStore encryptionStore = KeyStoreEncryptionStore.build(keystoreFile, secret);
		bind(EncryptionStore.class).toInstance(encryptionStore);

		return encryptionStore;
	}

}
