package org.platformlayer.service.gitosis.ops;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import org.platformlayer.ImageFactory;
import org.platformlayer.InstanceSupervisor;
import org.platformlayer.KeyPairUtils;

import org.apache.log4j.Logger;
import org.platformlayer.conductor.Tag;
import org.platformlayer.ops.CloudContext;
import org.platformlayer.ops.OpenstackComputeMachine;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.helpers.PersistentInstances;
import org.platformlayer.ops.helpers.ServiceContext;
import org.platformlayer.ops.helpers.SshKey;
import org.platformlayer.ops.helpers.SshKeys;
import org.platformlayer.ops.process.ProcessExecution;
import org.platformlayer.service.gitosis.model.GitServer;
import org.platformlayer.xaas.model.Managed;

import org.platformlayer.service.imagefactory.v1.DiskImageRecipe;
import org.platformlayer.service.instancesupervisor.v1.PersistentInstance;

public class GitServerController {
    static final Logger log = Logger.getLogger(GitServerController.class);

    @Inject
    OpsContext opsContext;

    @Inject
    CloudContext cloud;

    @Inject
    ImageFactory imageFactory;

    @Inject
    InstanceSupervisor instanceSupervisor;

    @Inject
    ServiceContext service;

    @Inject
    PersistentInstances persistentInstances;

    @Inject
    SshKeys sshKeys;

    public void initializeService() throws OpsException {
        String securityGroup = service.getSecurityGroupName();
        cloud.ensureCreatedSecurityGroup(securityGroup);
        cloud.ensurePortOpen(securityGroup, "tcp", 22);
    }

    public void doOperation(Managed<GitServer> managed) throws OpsException, IOException {
        initializeService();

        GitServer model = (GitServer) managed.getModel();

        Tag tag = new Tag(Tag.CONDUCTOR_ID, managed.getConductorId());

        SshKey sshKey = service.getSshKey();

        DiskImageRecipe recipe = imageFactory.loadDiskImageResource(getClass(), "DiskImageRecipe.xml");
        String securityGroup = service.getSecurityGroupName();

        int minimumMemoryMB = 256; // Git isn't particularly memory intensive (?)
        Managed<PersistentInstance> foundPersistentInstance = persistentInstances.getOrCreate(tag, recipe, model.dnsName, sshKey.getName(), securityGroup, minimumMemoryMB);

        OpenstackComputeMachine machine = persistentInstances.getMachine(foundPersistentInstance);

        OpsTarget target = machine.getTarget(sshKey);

        // target.mkdir(new File("/opt/scripts"));
        // target.setFileContents(new File("/opt/scripts/dnsdatabasemonitor"),
        // ResourceUtils.loadString(getClass(), "dnsdatabasemonitor"));
        // target.setFileContents(new
        // File("/etc/monit/conf.d/dnsdatabasemonitor"),
        // ResourceUtils.loadString(getClass(), "monitrc"));

        String adminUser = "gitadmin";
        target.executeCommand("adduser --group --system {0}", adminUser);

        File adminHomeDir = new File("/home", adminUser);
        File adminSshDir = new File(adminHomeDir, ".ssh");
        File privateKeyFile = new File(adminSshDir, "id_rsa");
        File publicKeyFile = new File(adminSshDir, "id_rsa.pub");
        File authorizedKeys = new File(adminSshDir, "authorized_keys");

        target.mkdir(adminSshDir);

        String passphrase = "";
        target.executeCommand("ssh-keygen -t rsa -f {0} -P {1}", privateKeyFile, passphrase);

        String privateKeyData = target.readTextFile(privateKeyFile);
        String publicKeyData = target.readTextFile(publicKeyFile);

        target.executeCommand("cat {0} | sudo -H -u gitosis gitosis-init", publicKeyFile);

        target.setFileContents(authorizedKeys, publicKeyData);
        target.executeCommand("chown -R {0} {1}", adminUser, adminSshDir);
        target.executeCommand("chmod -R 600 {0}", adminSshDir);
        target.executeCommand("chmod 700 {0}", adminSshDir);

        target.executeCommand("chsh -s /bin/bash {0}", adminUser);

        SshKey adminSshKey = new SshKey(null, adminUser, KeyPairUtils.deserialize(privateKeyData));

        OpsTarget adminTarget = machine.getTarget(adminSshKey);
        {
            ProcessExecution execution = adminTarget.executeCommand("ssh-keyscan 127.0.0.1");
            File knownHosts = new File(adminSshDir, "known_hosts");
            adminTarget.setFileContents(knownHosts, execution.getStdOut());
        }

        // adminTarget.executeCommand("git clone gitosis@127.0.0.1:gitosis-admin.git /home/gitadmin/gitosis-admin");

        // adminSshKey.
        //
        // adminTarget.executeCommand("git clone git@)
        // git clone git@YOUR_SERVER_HOSTNAME:gitosis-admin.git
        // cd gitosis-admin

    }
}
