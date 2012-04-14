package org.platformlayer.keystone.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.kohsuke.args4j.Option;
import org.openstack.utils.Io;
import org.openstack.utils.NoCloseInputStream;

import com.fathomdb.cli.CliOptions;

public class KeystoneCliOptions extends CliOptions {
	@Option(name = "-c", aliases = "--config", usage = "specify configuration file")
	String configFile;

	@Option(name = "-u", aliases = "--username", usage = "login username")
	String username;

	@Option(name = "-p", aliases = "--password", usage = "login password")
	String password;

	Properties config;

	public Properties getConfigurationProperties() {
		if (config == null) {
			Properties build = new Properties();

			if (configFile != null) {
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

					try {
						build.load(is);
					} catch (IOException e) {
						throw new IOException("Error reading configuration file", e);
					}
				} catch (IOException e) {
					throw new IllegalArgumentException("Error reading configuration file", e);
				} finally {
					Io.safeClose(is);
				}
			}

			if (this.username != null) {
				build.setProperty("platformlayer.username", this.username);
			}
			if (this.password != null) {
				build.setProperty("platformlayer.password", this.password);
			}

			this.config = build;
		}

		return config;
	}

	public String getUsername() {
		return getConfigurationProperties().getProperty("platformlayer.username");
	}

	public String getPassword() {
		return getConfigurationProperties().getProperty("platformlayer.password");
	}

}
