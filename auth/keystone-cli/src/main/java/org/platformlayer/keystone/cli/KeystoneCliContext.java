package org.platformlayer.keystone.cli;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Properties;

import org.openstack.crypto.KeyStoreUtils;
import org.platformlayer.RepositoryException;
import org.platformlayer.auth.KeystoneJdbcModule;
import org.platformlayer.auth.UserDatabase;
import org.platformlayer.auth.UserEntity;
import org.platformlayer.config.ConfigurationImpl;
import org.platformlayer.config.ConfigurationModule;
import org.platformlayer.crypto.CertificateReader;
import org.platformlayer.keystone.cli.commands.KeystoneCommandRegistry;
import org.platformlayer.keystone.cli.formatters.KeystoneFormatterRegistry;
import org.platformlayer.metrics.NullMetricsModule;
import org.platformlayer.ops.OpsException;

import com.fathomdb.cli.CliContextBase;
import com.fathomdb.cli.CliException;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public class KeystoneCliContext extends CliContextBase {
	final KeystoneCliOptions options;
	private Injector injector;

	public KeystoneCliContext(KeystoneCommandRegistry commandRegistry, KeystoneCliOptions options) {
		super(commandRegistry, new KeystoneFormatterRegistry());
		this.options = options;
	}

	@Override
	public void connect() throws Exception {
		Properties properties = options.getConfigurationProperties();
		ConfigurationImpl configuration = ConfigurationImpl.from(new File("."), properties);

		List<Module> modules = Lists.newArrayList();

		modules.add(new ConfigurationModule(configuration));
		modules.add(new KeystoneJdbcModule());
		modules.add(new NullMetricsModule());

		this.injector = Guice.createInjector(modules);
	}

	public UserDatabase getUserRepository() {
		try {
			return injector.getInstance(UserDatabase.class);
		} catch (ConfigurationException e) {
			throw new CliException("Database not configured; must set auth.system.module in configuration");
		}
	}

	/**
	 * Logs in the current user, directly accessing the database
	 */
	public UserEntity loginDirect() throws RepositoryException {
		String username = options.getUsername();
		String password = options.getPassword();
		if (username == null || password == null) {
			throw new IllegalArgumentException("Must specify username & password");
		}

		UserEntity user = (UserEntity) getUserRepository().authenticateWithPassword(username, password);
		if (user == null) {
			throw new SecurityException("Credentials were not valid");
		}
		return user;
	}

	public KeystoneCliOptions getOptions() {
		return options;
	}

	public Certificate[] getCertificateChain(String keystore, String keystoreSecret, String keyAlias)
			throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		if (getOptions().isServerMode()) {
			throw new IllegalArgumentException("Files not supported in server mode");
		}

		if (keystoreSecret == null) {
			keystoreSecret = KeyStoreUtils.DEFAULT_KEYSTORE_SECRET;
		}

		KeyStore keyStore = KeyStoreUtils.load(new File(keystore), keystoreSecret);

		if (keyAlias == null) {
			List<String> keyAliases = KeyStoreUtils.getKeyAliases(keyStore);
			if (keyAliases.size() == 0) {
				throw new CliException("No keys found in keystore");
			}
			if (keyAliases.size() != 1) {
				System.out.println("Found keys:\n\t" + Joiner.on("\n\t").join(keyAliases));
				throw new CliException("Multiple keys found in keystore; specify --alias");
			}

			keyAlias = keyAliases.get(0);
		}

		Certificate[] certificateChain = keyStore.getCertificateChain(keyAlias);

		return certificateChain;
	}

	public Certificate[] loadCertificateChain(String certPath) throws IOException, OpsException {
		if (getOptions().isServerMode()) {
			throw new IllegalArgumentException("Files not supported in server mode");
		}

		CertificateReader reader = new CertificateReader();
		Certificate[] certificates = reader.parse(new File(certPath));
		return certificates;
	}
}
