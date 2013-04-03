package org.platformlayer.keystone.cli.commands;

import java.util.List;

import org.kohsuke.args4j.Option;
import org.platformlayer.RepositoryException;
import org.platformlayer.auth.ServiceAccountEntity;
import org.platformlayer.auth.UserDatabase;

import com.fathomdb.utils.Hex;

public class ListServiceAccounts extends KeystoneCommandRunnerBase {
	@Option(name = "-k", aliases = "--key", usage = "Public key")
	public String publicKey;

	public ListServiceAccounts() {
		super("list", "serviceaccounts");
	}

	@Override
	public Object runCommand() throws RepositoryException {
		UserDatabase userRepository = getContext().getUserRepository();

		byte[] publicKeyBytes = null;
		if (publicKey != null) {
			publicKeyBytes = Hex.fromHex(publicKey);
		}
		List<ServiceAccountEntity> serviceAcccounts = userRepository.listAllServiceAccounts(publicKeyBytes);
		return serviceAcccounts;
	}

}
