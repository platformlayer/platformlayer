package org.platformlayer.client.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.kohsuke.args4j.Option;
import org.openstack.utils.NoCloseInputStream;
import org.platformlayer.DirectPlatformLayerClient;
import org.platformlayer.IoUtils;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.ops.OpsException;

import com.fathomdb.cli.CliOptions;

public class ConfigurationOptions extends CliOptions {
    @Option(name = "-c", aliases = "--config", usage = "config file", required = true)
    String configFile;

    public PlatformLayerClient buildPlatformLayerClient() throws IOException, OpsException {
        PlatformLayerClient client;
        if (configFile == null) {
            throw new IllegalArgumentException("Config file is required");
        }

        InputStream is = null;
        try {
            if (configFile.equals("-")) {
                // Read from stdin
                // Don't auto-close it, and that terminates nailgun
                is = new NoCloseInputStream(System.in);
            } else {
                if (isServerMode()) {
                    throw new IllegalArgumentException("Must pass config file over stdin in server mode");
                }
                File file = new File(configFile);
                if (!file.exists())
                    throw new FileNotFoundException("Configuration file not found: " + file);

                is = new FileInputStream(file);
            }

            Properties properties = new Properties();
            try {
                properties.load(is);
            } catch (IOException e) {
                throw new IOException("Error reading configuration file", e);
            }
            client = DirectPlatformLayerClient.buildUsingProperties(properties);

            // client = FederatedPlatformLayerClient.buildUsingConfig(is);
        } finally {
            if (is != System.in) {
                IoUtils.safeClose(is);
            }
        }

        return client;
    }

}
