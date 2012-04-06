package org.platformlayer.service.memcached.ops;

import java.io.IOException;

import javax.inject.Inject;

import org.platformlayer.ImageFactory;
import org.platformlayer.InstanceSupervisor;

import org.apache.log4j.Logger;
import org.platformlayer.conductor.Tag;
import org.platformlayer.ops.CloudContext;
import org.platformlayer.ops.OpenstackComputeMachine;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.helpers.PersistentInstances;
import org.platformlayer.ops.helpers.ServiceContext;
import org.platformlayer.ops.helpers.SshKey;
import org.platformlayer.service.memcached.model.MemcachedService;
import org.platformlayer.xaas.model.Managed;

import org.platformlayer.service.imagefactory.v1.DiskImageRecipe;
import org.platformlayer.service.instancesupervisor.v1.PersistentInstance;

public class MemcachedServiceController {
    static final Logger log = Logger.getLogger(MemcachedServiceController.class);

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

    public MemcachedServiceController() {
    }

    public void doOperation(Managed<MemcachedService> managed) throws OpsException, IOException {
        String key = managed.getConductorId();

        SshKey sshKey = service.getSshKey();

        Tag tag = new Tag(Tag.CONDUCTOR_ID, key);

        DiskImageRecipe recipe = imageFactory.loadDiskImageResource(getClass(), "DiskImageRecipe.xml");
        String securityGroup = service.getSecurityGroupName();

        // TODO: This needs to be configurable. Use tags?
        int minimumMemoryMb = 2048;
        Managed<PersistentInstance> foundPersistentInstance = persistentInstances.getOrCreate(tag, recipe, managed.getModel().dnsName, sshKey.getName(), securityGroup, minimumMemoryMb);

        OpenstackComputeMachine machine = persistentInstances.getMachine(foundPersistentInstance);
        // KeyPair sshKey = sshKeys.getOrCreate(sshKeyName);
        // OpsTarget target = machine.getTarget(sshKey);
        //
        // target.mkdir(new File("/opt/scripts"));
        // target.setFileContents(new File("/opt/scripts/dnsdatabasemonitor"),
        // ResourceUtils.loadString(getClass(), "dnsdatabasemonitor"));
        // target.setFileContents(new
        // File("/etc/monit/conf.d/dnsdatabasemonitor"),
        // ResourceUtils.loadString(getClass(), "monitrc"));
    }

}
