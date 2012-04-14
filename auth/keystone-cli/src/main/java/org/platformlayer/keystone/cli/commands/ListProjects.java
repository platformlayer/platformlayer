package org.platformlayer.keystone.cli.commands;

import org.kohsuke.args4j.Argument;
import org.platformlayer.RepositoryException;
import org.platformlayer.auth.OpsUser;
import org.platformlayer.auth.UserRepository;
import org.platformlayer.keystone.cli.model.UserName;

public class ListProjects extends KeystoneCommandRunnerBase {
	@Argument(index = 0, required = true, metaVar = "username", usage = "Name of user")
	public UserName username;

	public ListProjects() {
		super("list", "projects");
	}

	@Override
	public Object runCommand() throws RepositoryException {
		UserRepository userRepository = getContext().getUserRepository();

		// if (username == null) {
		// return userRepository.listAllProjectNames(null);
		// } else {
		OpsUser user = userRepository.findUser(username.getKey());
		if (user == null) {
			throw new IllegalArgumentException("User not found");
		}
		return userRepository.listProjectsByUserId(user.id);
		// }
	}

}
