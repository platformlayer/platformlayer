package org.platformlayer.keystone.cli.commands;

import java.util.List;

import org.kohsuke.args4j.Argument;
import org.platformlayer.RepositoryException;
import org.platformlayer.auth.UserRepository;

public class ListUsers extends KeystoneCommandRunnerBase {
	@Argument(index = 0)
	public String prefix;

	public ListUsers() {
		super("list", "users");
	}

	@Override
	public Object runCommand() throws RepositoryException {
		UserRepository userRepository = getContext().getUserRepository();

		List<String> users = userRepository.listAllUserNames(prefix);
		return users;
	}

}
