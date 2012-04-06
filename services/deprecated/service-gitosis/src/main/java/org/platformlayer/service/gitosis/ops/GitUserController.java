package org.platformlayer.service.gitosis.ops;

import javax.inject.Inject;


import org.apache.log4j.Logger;
import org.platformlayer.service.gitosis.model.GitUser;
import org.platformlayer.xaas.model.Managed;

public class GitUserController {
    static final Logger log = Logger.getLogger(GitUserController.class);

    @Inject
    GitHelpers git;

    public void doOperation(Managed<GitUser> managed) throws Exception {
        GitUser model = (GitUser) managed.getModel();

        AddUserOperation operation = new AddUserOperation(model);

        git.doForAllServers(operation);
    }
}
