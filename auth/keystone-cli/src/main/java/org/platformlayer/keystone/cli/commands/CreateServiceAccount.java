package org.platformlayer.keystone.cli.commands;

import java.io.File;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.openstack.crypto.KeyStoreUtils;
import org.openstack.keystone.services.ServiceAccount;
import org.platformlayer.auth.UserDatabase;

public class CreateServiceAccount extends KeystoneCommandRunnerBase {
	@Argument(index = 0, required = true, metaVar = "keystore file")
	public String keystoreFile;
	@Argument(index = 1, required = true, metaVar = "secret")
	public String secret;

	public CreateServiceAccount() {
		super("create", "serviceaccount");
	}

	@Override
	public Object runCommand() throws Exception {
		KeyStore keystore = KeyStoreUtils.load(new File(keystoreFile), secret);

		List<Certificate[]> certificateChains = KeyStoreUtils.getCertificateChains(keystore);
		if (certificateChains.size() != 1) {
			// Easyish to add support for this when we need it
			throw new IllegalStateException("Only single-entry keystores are supported (currently)");
		}

		Certificate[] certificateChain = certificateChains.get(0);

		X509Certificate cert = (X509Certificate) certificateChain[0];

		UserDatabase userRepository = getContext().getUserRepository();

		ServiceAccount account = userRepository.createServiceAccount(cert);

		return account;
	}

}
