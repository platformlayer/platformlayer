package org.platformlayer.keystone.cli.commands;

import org.kohsuke.args4j.Argument;
import org.platformlayer.RepositoryException;
import org.platformlayer.auth.UserDatabase;
import org.platformlayer.auth.UserEntity;
import org.platformlayer.keystone.cli.model.UserName;

public class ListProjects extends KeystoneCommandRunnerBase {
	@Argument(index = 0, required = true, metaVar = "username", usage = "Name of user")
	public UserName username;

	public ListProjects() {
		super("list", "projects");
	}

	@Override
	public Object runCommand() throws RepositoryException {
		UserDatabase userRepository = getContext().getUserRepository();

		// if (username == null) {
		// return userRepository.listAllProjectNames(null);
		// } else {
		UserEntity user = (UserEntity) userRepository.findUser(username.getKey());
		if (user == null) {
			throw new IllegalArgumentException("User not found");
		}
		return userRepository.listProjectsByUserId(user.id);
		// }
	}

}
