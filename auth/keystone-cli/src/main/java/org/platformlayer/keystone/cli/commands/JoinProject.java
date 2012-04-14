package org.platformlayer.keystone.cli.commands;

import java.io.IOException;

import javax.crypto.SecretKey;

import org.kohsuke.args4j.Argument;
import org.platformlayer.RepositoryException;
import org.platformlayer.auth.OpsProject;
import org.platformlayer.auth.OpsUser;
import org.platformlayer.auth.UserRepository;
import org.platformlayer.auth.crypto.SecretStore;
import org.platformlayer.keystone.cli.model.ProjectName;
import org.platformlayer.keystone.cli.model.UserName;

public class JoinProject extends KeystoneCommandRunnerBase {
	@Argument(index = 0, required = true)
	public UserName username;

	@Argument(index = 1, required = true)
	public ProjectName projectKey;

	public JoinProject() {
		super("join", "project");
	}

	@Override
	public Object runCommand() throws RepositoryException, IOException {
		UserRepository userRepository = getContext().getUserRepository();

		OpsUser me = getContext().login();
		OpsProject project = userRepository.findProjectByKey(projectKey.getKey());
		if (project == null) {
			throw new IllegalArgumentException("Project not found");
		}

		SecretStore secretStore = new SecretStore(project.secretData);
		SecretKey projectKey = secretStore.getSecretFromUser(me);

		userRepository.addUserToProject(username.getKey(), project.key, projectKey);

		return project;
	}

}
