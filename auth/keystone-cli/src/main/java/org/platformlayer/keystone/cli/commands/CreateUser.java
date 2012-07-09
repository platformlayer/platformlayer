package org.platformlayer.keystone.cli.commands;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.platformlayer.RepositoryException;
import org.platformlayer.auth.OpsUser;
import org.platformlayer.auth.UserDatabase;

import com.fathomdb.cli.CliException;

public class CreateUser extends KeystoneCommandRunnerBase {
	@Argument(index = 0, required = true)
	public String username;

	@Option(name = "-p", aliases = "--password", usage = "password")
	public String password;

	@Option(name = "-k", aliases = "--key", usage = "keystore")
	public String keystore;

	@Option(name = "-s", aliases = "--secret", usage = "keystore secret")
	public String keystoreSecret;

	@Option(name = "-a", aliases = "--alias", usage = "key alias")
	public String keyAlias;

	public CreateUser() {
		super("create", "user");
	}

	@Override
	public Object runCommand() throws RepositoryException, GeneralSecurityException, IOException {
		if (password == null && keystore == null) {
			throw new CliException("Either key or password is required");
		}

		UserDatabase userRepository = getContext().getUserRepository();
		Certificate[] certificateChain = null;

		if (keystore != null) {
			certificateChain = getContext().getCertificateChain(keystore, keystoreSecret, keyAlias);
		}

		OpsUser user = userRepository.createUser(username, password, certificateChain);
		return user;
	}

}
