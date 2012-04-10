package org.platformlayer.service.git.ops;

import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.ManagedItemState;
import org.platformlayer.ops.CustomRecursor;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.helpers.ServiceContext;
import org.platformlayer.ops.helpers.SshKey;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.git.model.GitRepository;
import org.platformlayer.service.git.model.GitService;

public class GitServerAssignment extends OpsTreeBase implements CustomRecursor {
    static final Logger log = Logger.getLogger(GitServerAssignment.class);

    @Inject
    PlatformLayerHelpers platformLayer;

    @Inject
    InstanceHelpers instances;

    @Inject
    ServiceContext service;

    @Handler
    public void handler(GitRepository model) throws Exception {
        // TODO: Support backup on that GitServer
        List<GitService> gitServices = platformLayer.listItems(GitService.class);

        if (gitServices.size() != 1) {
            // TODO: Assign to a single git server
            throw new OpsException("Only 1 git server implemented at the moment");
        }

        GitService gitService = gitServices.get(0);
        if (gitService.getState() != ManagedItemState.ACTIVE) {
            throw new OpsException("Server not yet active: " + gitService);
        }

        Machine machine = instances.findMachine(gitService);
        if (machine == null) {
            throw new OpsException("Server machine not found:" + gitService);
        }

        SshKey sshKey = service.getSshKey();
        OpsTarget target = machine.getTarget(sshKey);

        getRecursionState().pushChildScope(OpsTarget.class, target);
    }

    @Override
    protected void addChildren() throws OpsException {

    }

}
