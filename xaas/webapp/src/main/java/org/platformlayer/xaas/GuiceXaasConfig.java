package org.platformlayer.xaas;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.TrustManager;
import javax.sql.DataSource;

import org.openstack.crypto.KeyStoreUtils;
import org.openstack.keystone.service.KeystoneTokenValidator;
import org.openstack.utils.PropertyUtils;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.WellKnownPorts;
import org.platformlayer.auth.UserRepository;
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
import org.platformlayer.ops.ssh.ISshContext;
import org.platformlayer.ops.tasks.OperationQueue;
import org.platformlayer.ops.tasks.SimpleOperationQueue;
import org.platformlayer.ssh.mina.MinaSshContext;
import org.platformlayer.xaas.discovery.AnnotationDiscovery;
import org.platformlayer.xaas.discovery.JerseyAnnotationDiscovery;
import org.platformlayer.xaas.keystone.KeystoneUserRepository;
import org.platformlayer.xaas.ops.InProcessChangeQueue;
import org.platformlayer.xaas.repository.JobRepository;
import org.platformlayer.xaas.repository.ManagedItemRepository;
import org.platformlayer.xaas.repository.ServiceAuthorizationRepository;
import org.platformlayer.xaas.services.AnnotationServiceProviderDictionary;
import org.platformlayer.xaas.services.ChangeQueue;
import org.platformlayer.xaas.services.ServiceProviderDictionary;
import org.platformlayer.xaas.web.jaxrs.JaxbContextHelper;

import com.google.common.base.Splitter;
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

		OpsConfiguration configuration;
		try {
			configuration = new OpsConfiguration(applicationProperties);
			bind(OpsConfiguration.class).toInstance(configuration);
		} catch (OpsException e) {
			throw new IllegalStateException("Cannot load system configuration", e);
		}

		configuration.bindProperties(binder());

		bind(ISshContext.class).to(MinaSshContext.class);

		bind(OpsSystem.class);

		bind(OpsContext.class).toProvider(OpsContextProvider.class);
		bind(UserRepository.class).to(KeystoneUserRepository.class).asEagerSingleton();

		bind(ResultSetMappers.class).toProvider(ResultSetMappersProvider.build(ItemEntity.class, TagEntity.class));

		bind(DataSource.class).toProvider(new GuiceDataSourceProvider("platformlayer.jdbc.", null));

		JerseyAnnotationDiscovery discovery = new JerseyAnnotationDiscovery();
		discovery.scan();
		bind(AnnotationDiscovery.class).toInstance(discovery);

		bind(ExecutorService.class).toInstance(Executors.newCachedThreadPool());

		File base = new File(".").getAbsoluteFile();
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

		{
			String keystoneServiceUrl = configuration.lookup("auth.system.url", "https://127.0.0.1:"
					+ WellKnownPorts.PORT_PLATFORMLAYER_AUTH_ADMIN + "/");
			// String keystoneServiceToken = "auth_token";

			String certFilePath = configuration.lookup("auth.system.cert", "keystore.jks");
			String secret = configuration.lookup("auth.system.cert.password", "notasecret");

			File certFile = new File(certFilePath);

			if (!certFile.exists()) {
				throw new IllegalArgumentException("Certificate file not found: " + certFile.getAbsolutePath());
			}
			// KeyStore keyStore = KeyStore.getInstance("PKCS12");
			// keyStore.load(new FileInputStream(privateKeyFile), privateKeyPassword.toCharArray());

			// InputStream keyInput = new FileInputStream(pKeyFile);
			// keyStore.load(keyInput, pKeyPassword.toCharArray());
			// keyInput.close();

			HostnameVerifier hostnameVerifier = null;

			// We need to pass a keystore password, though I don't think it's used
			// String keystorePassword = "password";
			ClientCertificateKeyManager keyManager = null;
			try {
				KeyStore keystore = KeyStoreUtils.load(certFile, secret);

				keyManager = new ClientCertificateKeyManager(keystore, secret);

				// System.out.println("Keystore has " + keystore.size() + " keys");
				// clientCertificateInfo = new KeystoreInfo(clientCert, secret);
			} catch (Exception e) {
				throw new IllegalArgumentException("Error loading client certificate", e);
			}

			TrustManager trustManager = null;

			String trustKeys = configuration.lookup("auth.system.key", null);

			if (trustKeys != null) {
				trustManager = new PublicKeyTrustManager(Splitter.on(',').trimResults().split(trustKeys));

				hostnameVerifier = new AcceptAllHostnameVerifier();
			}

			KeystoneTokenValidator keystoneTokenValidator = new KeystoneTokenValidator(keystoneServiceUrl, keyManager,
					trustManager, hostnameVerifier);

			bind(KeystoneTokenValidator.class).toInstance(keystoneTokenValidator);
		}

	}

}
