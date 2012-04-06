package org.platformlayer.service.gitosis.ops;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import org.platformlayer.KeyPairUtils;

import org.apache.log4j.Logger;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.TimeSpan;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpenstackComputeMachine;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.helpers.ServiceContext;
import org.platformlayer.ops.helpers.SshKey;
import org.platformlayer.service.gitosis.model.GitServer;
import org.platformlayer.xaas.model.Managed;
import org.platformlayer.xaas.model.ManagedItemState;

public class GitHelpers {
    static final Logger log = Logger.getLogger(GitHelpers.class);

    @Inject
    PlatformLayerClient platformLayer;

    @Inject
    InstanceHelpers instances;

    @Inject
    ServiceContext service;

    OpsTarget getAdminTarget(OpsTarget rootTarget, Machine machine) throws OpsException, IOException {
        String adminUser = "gitadmin";

        File adminHomeDir = new File("/home", adminUser);
        File adminSshDir = new File(adminHomeDir, ".ssh");
        File privateKeyFile = new File(adminSshDir, "id_rsa");
        // File publicKeyFile = new File(adminSshDir, "id_rsa.pub");
        // File authorizedKeys = new File(adminSshDir, "authorized_keys");

        String privateKeyData = rootTarget.readTextFile(privateKeyFile);

        SshKey adminSshKey = new SshKey(null, adminUser, KeyPairUtils.deserialize(privateKeyData));

        return machine.getTarget(adminSshKey);
    }

    void doOperation(OpsTarget target, GitosisOperation operation) throws Exception {
        File tempDir = target.createTempDir();

        // TODO: Remove tempDir even if we throw an exception

        // By cloning the repo every time, we don't have to worry about the repo being in an odd state
        // A concurrent operation will simply fail to push and we'll auto-retry
        target.executeCommand("git clone gitosis@127.0.0.1:gitosis-admin.git {0}", tempDir);

        operation.doOperation(target, tempDir);

        target.executeCommand("cd {0}; git add --all", tempDir);
        target.executeCommand("cd {0}; git commit -a -m {1}", tempDir, operation.getCommitMessage());
        target.executeCommand("cd {0}; git push origin master", tempDir);

        target.rmdir(tempDir);
    }

    public void doForAllServers(GitosisOperation operation) throws Exception {
        boolean failed = false;

        for (Managed<GitServer> gitServer : platformLayer.listItems(GitServer.class)) {
            if (gitServer.getState() != ManagedItemState.ACTIVE) {
                log.warn("Server not yet active: " + gitServer);
                failed = true;
                continue;
            }

            OpenstackComputeMachine machine = instances.findMachine(gitServer);
            if (machine == null) {
                log.warn("Server instance not found: " + gitServer);
                failed = true;
                continue;
            }

            SshKey sshKey = service.getSshKey();
            OpsTarget rootTarget = machine.getTarget(sshKey);

            OpsTarget adminTarget = getAdminTarget(rootTarget, machine);
            doOperation(adminTarget, operation);
        }

        if (failed) {
            throw new OpsException("Could not update all DNS servers in cluster").setRetry(TimeSpan.ONE_MINUTE);
        }
    }

}
