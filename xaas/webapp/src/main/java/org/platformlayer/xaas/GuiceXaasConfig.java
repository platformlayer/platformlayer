package org.platformlayer.xaas;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import org.openstack.utils.PropertyUtils;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.auth.JdbcUserRepository;
import org.platformlayer.auth.OpsProject;
import org.platformlayer.auth.OpsUser;
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
		File file = new File("configuration.properties");
		Properties applicationProperties;
		try {
			applicationProperties = PropertyUtils.loadProperties(file);
		} catch (IOException e) {
			throw new IllegalStateException("Error loading configuration file: " + file, e);
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
		bind(UserRepository.class).to(JdbcUserRepository.class).asEagerSingleton();

		bind(ResultSetMappers.class).toProvider(
				ResultSetMappersProvider.build(OpsUser.class, OpsProject.class, ItemEntity.class, TagEntity.class));

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
	}

}
