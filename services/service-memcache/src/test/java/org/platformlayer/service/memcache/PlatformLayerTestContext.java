package org.platformlayer.service.memcache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.openstack.utils.Io;
import org.platformlayer.DirectPlatformLayerClient;
import org.platformlayer.IoUtils;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.TypedItemMapper;
import org.platformlayer.TypedPlatformLayerClient;
import org.platformlayer.ops.OpsException;

public class PlatformLayerTestContext {
    PlatformLayerClient platformLayerClient;

    TypedPlatformLayerClient typedClient;

    final File configFile;

    final TypedItemMapper typedItemMapper;

    public PlatformLayerTestContext(File configFile, TypedItemMapper typedItemMapper) {
        this.configFile = configFile;
        this.typedItemMapper = typedItemMapper;
    }

    public static PlatformLayerTestContext buildFromProperties(TypedItemMapper typedItemMapper) {
        String config = System.getProperty("config");
        if (config == null) {
            config = "tests";
        }

        if (!config.contains(File.separator)) {
            config = "~/.credentials/" + config;
        }

        File configFile = Io.resolve(config);
        return new PlatformLayerTestContext(configFile, typedItemMapper);
    }

    public PlatformLayerClient buildPlatformLayerClient() throws IOException, OpsException {
        PlatformLayerClient client;
        if (configFile == null) {
            throw new IllegalArgumentException("Config file is required");
        }

        InputStream is = null;
        try {
            if (!configFile.exists()) {
                throw new FileNotFoundException("Configuration file not found: " + configFile);
            }

            is = new FileInputStream(configFile);

            Properties properties = new Properties();
            try {
                properties.load(is);
            } catch (IOException e) {
                throw new IOException("Error reading configuration file", e);
            }
            client = DirectPlatformLayerClient.buildUsingProperties(properties);
        } finally {
            if (is != System.in) {
                IoUtils.safeClose(is);
            }
        }

        return client;
    }

    public PlatformLayerClient getUntypedClient() throws IOException, OpsException {
        if (platformLayerClient == null) {
            platformLayerClient = buildPlatformLayerClient();
        }
        return platformLayerClient;
    }

    public TypedPlatformLayerClient getTypedClient() throws IOException, OpsException {
        if (typedClient == null) {
            typedClient = new TypedPlatformLayerClient(getUntypedClient(), getMapper());
        }
        return typedClient;
    }

    private TypedItemMapper getMapper() {
        return typedItemMapper;
    }
}
