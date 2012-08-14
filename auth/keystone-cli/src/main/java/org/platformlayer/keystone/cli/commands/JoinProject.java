package org.platformlayer.keystone.cli.commands;

import java.io.IOException;
import java.util.Collections;

import javax.crypto.SecretKey;

import org.kohsuke.args4j.Argument;
import org.platformlayer.RepositoryException;
import org.platformlayer.auth.ProjectEntity;
import org.platformlayer.auth.UserDatabase;
import org.platformlayer.auth.UserEntity;
import org.platformlayer.auth.crypto.SecretStore;
import org.platformlayer.keystone.cli.model.ProjectName;
import org.platformlayer.keystone.cli.model.UserName;
import org.platformlayer.model.RoleId;

import com.fathomdb.cli.CliException;
import com.google.common.base.Strings;

public class JoinProject extends KeystoneCommandRunnerBase {
	@Argument(index = 0, required = true, metaVar = "username")
	public UserName username;

	@Argument(index = 1, required = true, metaVar = "project")
	public ProjectName projectKey;

	@Argument(index = 2, required = true, metaVar = "role")
	public String roleKey;

	public JoinProject() {
		super("join", "project");
	}

	@Override
	public Object runCommand() throws RepositoryException, IOException {
		UserDatabase userRepository = getContext().getUserRepository();

		UserEntity me = getContext().loginDirect();
		ProjectEntity project = userRepository.findProjectByKey(projectKey.getKey());
		if (project == null) {
			throw new CliException("Project not found: " + projectKey.getKey());
		}

		SecretStore secretStore = new SecretStore(project.secretData);
		SecretKey projectSecret = secretStore.getSecretFromUser(me);
		if (projectSecret == null) {
			String msg = "Cannot retrieve project secret.";
			msg += " Is " + me.key + " a member of " + project.getName() + "?";
			throw new CliException(msg);
		}
		if (Strings.isNullOrEmpty(roleKey)) {
			throw new CliException("Role is required");
		}
		RoleId role = new RoleId(roleKey);
		userRepository.addUserToProject(username.getKey(), project.getName(), projectSecret,
				Collections.singletonList(role));

		return project;
	}
}
