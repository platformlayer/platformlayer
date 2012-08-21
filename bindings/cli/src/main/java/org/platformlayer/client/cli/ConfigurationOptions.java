package org.platformlayer.client.cli;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;

import org.kohsuke.args4j.Option;
import org.openstack.utils.Io;
import org.openstack.utils.NoCloseInputStream;
import org.platformlayer.HttpPlatformLayerClient;
import org.platformlayer.IoUtils;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.http.HttpStrategy;
import org.platformlayer.http.jre.JreHttpStrategy;
import org.platformlayer.ops.OpsException;

import com.fathomdb.cli.CliException;
import com.fathomdb.cli.CliOptions;

public class ConfigurationOptions extends CliOptions {
	@Option(name = "-c", aliases = "--config", usage = "config file", required = true)
	String configFile;

	@Option(name = "-debug", aliases = "--debug", usage = "enable debug output")
	boolean debug;

	public PlatformLayerClient buildPlatformLayerClient() throws IOException, OpsException {
		HttpPlatformLayerClient client;
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
				File file = Io.resolve(configFile);
				if (!file.exists()) {
					throw new FileNotFoundException("Configuration file not found: " + file);
				}

				is = new FileInputStream(file);
			}

			Properties properties = new Properties();
			try {
				properties.load(is);
			} catch (IOException e) {
				throw new IOException("Error reading configuration file", e);
			}

			if (properties.getProperty("platformlayer.username") == null) {
				throw new CliException("User property not set in configuration file");
			}

			HttpStrategy httpStrategy = new JreHttpStrategy();
			client = HttpPlatformLayerClient.buildUsingProperties(httpStrategy, properties);

			if (debug) {
				client.setDebug(System.err);
			} else {
				// We don't want debug messages to interfere with our output
				// TODO: Fix this so debug output doesn't interfere (stderr?)
				// TODO: Maybe output the debug info only in case of failure?
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				client.setDebug(new PrintStream(baos));
			}

			// client = FederatedPlatformLayerClient.buildUsingConfig(is);
		} finally {
			if (is != System.in) {
				IoUtils.safeClose(is);
			}
		}

		return client;
	}

}
