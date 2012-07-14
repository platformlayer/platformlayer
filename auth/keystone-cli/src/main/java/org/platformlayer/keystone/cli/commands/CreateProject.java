package org.platformlayer.keystone.cli.commands;

import org.kohsuke.args4j.Argument;
import org.platformlayer.RepositoryException;
import org.platformlayer.auth.ProjectEntity;
import org.platformlayer.auth.UserDatabase;
import org.platformlayer.auth.UserEntity;

public class CreateProject extends KeystoneCommandRunnerBase {
	@Argument(index = 0, required = true)
	public String projectKey;

	public CreateProject() {
		super("create", "project");
	}

	@Override
	public Object runCommand() throws RepositoryException {
		UserDatabase userRepository = getContext().getUserRepository();

		// We need to login to unlock the user key so we can encrypt the project key!
		UserEntity me = getContext().loginDirect();

		ProjectEntity project = userRepository.createProject(projectKey, me);

		return project;
	}

}
