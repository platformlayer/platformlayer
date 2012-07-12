package org.platformlayer.keystone.cli.commands;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import org.kohsuke.args4j.Option;
import org.openstack.keystone.services.ServiceAccount;
import org.platformlayer.auth.UserDatabase;

public class CreateServiceAccount extends KeystoneCommandRunnerBase {
	@Option(name = "-k", aliases = "--key", usage = "keystore")
	public String keystore;

	@Option(name = "-s", aliases = "--secret", usage = "keystore secret")
	public String keystoreSecret;

	@Option(name = "-a", aliases = "--alias", usage = "key alias")
	public String keyAlias;

	public CreateServiceAccount() {
		super("create", "serviceaccount");
	}

	@Override
	public Object runCommand() throws Exception {
		Certificate[] certificateChain = getContext().getCertificateChain(keystore, keystoreSecret, keyAlias);

		X509Certificate cert = (X509Certificate) certificateChain[0];

		UserDatabase userRepository = getContext().getUserRepository();

		ServiceAccount account = userRepository.createServiceAccount(cert);

		return account;
	}

}
