package org.platformlayer.keystone.cli.commands;

import org.kohsuke.args4j.Argument;
import org.platformlayer.RepositoryException;
import org.platformlayer.auth.OpsUser;
import org.platformlayer.auth.UserDatabase;

public class CreateUser extends KeystoneCommandRunnerBase {
	@Argument(index = 0, required = true)
	public String username;

	@Argument(index = 1, required = true)
	public String password;

	public CreateUser() {
		super("create", "user");
	}

	@Override
	public Object runCommand() throws RepositoryException {
		UserDatabase userRepository = getContext().getUserRepository();

		OpsUser user = userRepository.createUser(username, password);

		return user;
	}

}
