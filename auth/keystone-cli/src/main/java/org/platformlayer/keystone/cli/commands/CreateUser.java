package org.platformlayer.keystone.cli.commands;

import org.kohsuke.args4j.Argument;
import org.platformlayer.RepositoryException;
import org.platformlayer.auth.OpsUser;
import org.platformlayer.auth.UserRepository;

public class CreateUser extends KeystoneCommandRunnerBase {
    @Argument(index = 0)
    public String username;

    @Argument(index = 1)
    public String password;

    public CreateUser() {
        super("create", "user");
    }

    @Override
    public Object runCommand() throws RepositoryException {
        UserRepository userRepository = getContext().getUserRepository();

        OpsUser user = userRepository.createUser(username, password);

        return user;
    }

}
