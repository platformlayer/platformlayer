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

import com.fathomdb.cli.CliException;

public class JoinProject extends KeystoneCommandRunnerBase {
	@Argument(index = 0, required = true, metaVar = "username")
	public UserName username;

	@Argument(index = 1, required = true, metaVar = "project")
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
			throw new CliException("Project not found: " + projectKey.getKey());
		}

		SecretStore secretStore = new SecretStore(project.secretData);
		SecretKey projectKey = secretStore.getSecretFromUser(me);
		if (projectKey == null) {
			String msg = "Cannot retrieve project secret.";
			msg += " Is " + me.key + " a member of " + project.key + "?";
			throw new CliException(msg);
		}
		userRepository.addUserToProject(username.getKey(), project.key, projectKey);

		return project;
	}

}
