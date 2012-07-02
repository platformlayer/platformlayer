package org.platformlayer.keystone.cli;

import org.platformlayer.RepositoryException;
import org.platformlayer.auth.UserDatabase;
import org.platformlayer.auth.UserEntity;
import org.platformlayer.keystone.cli.commands.KeystoneCommandRegistry;
import org.platformlayer.keystone.cli.formatters.KeystoneFormatterRegistry;
import org.platformlayer.keystone.cli.guice.CliModule;

import com.fathomdb.cli.CliContextBase;
import com.fathomdb.cli.CliException;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class KeystoneCliContext extends CliContextBase {
	final KeystoneCliOptions options;
	private Injector injector;

	public KeystoneCliContext(KeystoneCommandRegistry commandRegistry, KeystoneCliOptions options) {
		super(commandRegistry, new KeystoneFormatterRegistry());
		this.options = options;
	}

	@Override
	public void connect() throws Exception {
		this.injector = Guice.createInjector(new CliModule(options));
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
}
