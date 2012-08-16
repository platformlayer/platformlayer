package org.platformlayer.keystone.cli.commands;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.platformlayer.RepositoryException;
import org.platformlayer.auth.OpsUser;
import org.platformlayer.auth.UserDatabase;
import org.platformlayer.ops.OpsException;

import com.fathomdb.cli.CliException;

public class CreateUser extends KeystoneCommandRunnerBase {
	@Argument(index = 0, required = true, usage = "username")
	public String username;

	@Option(name = "-p", aliases = "--password", usage = "password")
	public String password;

	@Option(name = "-c", aliases = "--cert", usage = "certificate")
	public String certPath;

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
	public Object runCommand() throws RepositoryException, GeneralSecurityException, IOException, OpsException {
		if (password == null && keystore == null && certPath == null) {
			throw new CliException("Either key or password or cert is required");
		}

		UserDatabase userRepository = getContext().getUserRepository();
		Certificate[] certificateChain = null;

		if (keystore != null) {
			certificateChain = getContext().getCertificateChain(keystore, keystoreSecret, keyAlias);
		} else if (certPath != null) {
			certificateChain = getContext().loadCertificateChain(certPath);
		}

		OpsUser user = userRepository.createUser(username, password, certificateChain);
		return user;
	}

}
