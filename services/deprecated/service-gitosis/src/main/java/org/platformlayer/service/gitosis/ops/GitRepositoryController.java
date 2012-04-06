package org.platformlayer.service.gitosis.ops;

import javax.inject.Inject;


import org.apache.log4j.Logger;
import org.platformlayer.service.gitosis.model.GitRepository;
import org.platformlayer.xaas.model.Managed;

public class GitRepositoryController {
    static final Logger log = Logger.getLogger(GitRepositoryController.class);

    @Inject
    GitHelpers git;

    public void doOperation(Managed<GitRepository> managed) throws Exception {
        GitRepository model = (GitRepository) managed.getModel();

        AddRepositoryOperation operation = new AddRepositoryOperation(model);

        git.doForAllServers(operation);
    }
}
