package org.openstack.keystone.server;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.openstack.keystone.services.CacheSystem;
import org.openstack.keystone.services.GroupToTenantMapper;
import org.openstack.keystone.services.ServiceDictionary;
import org.openstack.keystone.services.TokenService;
import org.openstack.keystone.services.crypto.SharedSecretTokenService;
import org.openstack.keystone.services.memory.SimpleCacheSystem;
import org.openstack.keystone.services.memory.SimpleGroupToTenantMapper;
import org.openstack.keystone.services.memory.SimpleServiceDictionary;
import org.openstack.utils.PropertyUtils;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.name.Names;

public class GuiceAuthenticationConfig extends AbstractModule {

    @Override
    protected void configure() {
        String configFilePath = System.getProperty("conf");
        if (configFilePath == null) {
            configFilePath = new File(new File("."), "configuration.properties").getAbsolutePath();
        }

        File configFile = new File(configFilePath);
        File baseDir = configFile.getParentFile();
        Properties config;

        try {
            config = PropertyUtils.loadProperties(configFile);
        } catch (IOException e) {
            throw new IllegalStateException("Error loading configuration file: " + configFilePath, e);
        }

        String secret = config.getProperty("sharedsecret");
        if (secret == null) {
            throw new IllegalStateException("sharedsecret is required");
        }

        Names.bindProperties(binder(), config);

        bindAuthenticationModules(config);

        File serviceDirectory = new File(baseDir, "services");
        if (serviceDirectory.exists()) {
            SimpleServiceDictionary serviceDictionary = SimpleServiceDictionary.loadFromDirectory(serviceDirectory);

            bind(ServiceDictionary.class).toInstance(serviceDictionary);
        }

        TokenService tokenService = new SharedSecretTokenService(secret);
        bind(TokenService.class).toInstance(tokenService);

        int cacheSize = 1000;
        CacheSystem simpleCacheSystem = new SimpleCacheSystem(cacheSize);
        bind(CacheSystem.class).toInstance(simpleCacheSystem);

        GroupToTenantMapper groupToTenantMapper = new SimpleGroupToTenantMapper();
        bind(GroupToTenantMapper.class).toInstance(groupToTenantMapper);
    }

    private void bindAuthenticationModules(Properties config) {
        String userProvider = config.getProperty("auth.user.module");
        if (userProvider != null)
            installModule(userProvider);
        String systemProvider = config.getProperty("auth.system.module");
        if (systemProvider != null)
            installModule(systemProvider);
    }

    private void installModule(String moduleClassName) {
        try {
            Class<?> moduleClass = Class.forName(moduleClassName);
            Module module = (Module) moduleClass.newInstance();
            binder().install(module);
        } catch (Exception e) {
            throw new IllegalStateException("Error loading class: " + moduleClassName);
        }
    }

}
