package org.platformlayer.keystone.cli.commands;

import org.kohsuke.args4j.Argument;
import org.platformlayer.RepositoryException;
import org.platformlayer.auth.OpsProject;
import org.platformlayer.auth.OpsUser;
import org.platformlayer.auth.UserRepository;

public class CreateProject extends KeystoneCommandRunnerBase {
    @Argument(index = 0)
    public String projectKey;

    public CreateProject() {
        super("create", "project");
    }

    @Override
    public Object runCommand() throws RepositoryException {
        UserRepository userRepository = getContext().getUserRepository();

        // We need to login to unlock the user key so we can encrypt the project key!
        OpsUser me = getContext().login();

        OpsProject project = userRepository.createProject(projectKey, me);

        return project;
    }

}
