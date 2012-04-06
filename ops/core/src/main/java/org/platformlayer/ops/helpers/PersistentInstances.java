package org.platformlayer.ops.helpers;

import org.platformlayer.PlatformLayerClient;
import org.platformlayer.TimeSpan;
import org.platformlayer.core.model.InstanceBase;
import org.platformlayer.ops.CloudContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.service.instancesupervisor.v1.PersistentInstance;

import com.google.inject.Inject;

public class PersistentInstances {
    @Inject
    InstanceSupervisor instanceSupervisor;

    @Inject
    ImageFactory imageFactory;

    @Inject
    PlatformLayerClient platformLayer;

    @Inject
    CloudContext cloud;

    @Inject
    InstanceHelpers instances;

    // public PersistentInstance getOrCreate(Tag tag, DiskImageRecipe recipe, String dnsName, PublicKey sshPublicKey, String securityGroup, int minimumMemoryMB) throws OpenstackClientException,
    // OpsException {
    // PersistentInstance persistentInstance = new PersistentInstance();
    // persistentInstance.setDnsName(dnsName);
    //
    // persistentInstance.setSshPublicKey(SshKeys.serialize(sshPublicKey));
    //
    // persistentInstance.setSecurityGroup(securityGroup);
    // persistentInstance.setMinimumRam(minimumMemoryMB);
    //
    // return getOrCreate(tag, recipe, persistentInstance);
    // }

    public InstanceBase getInstance(PersistentInstance persistentInstance) throws OpsException {
        // We have to connect to the underlying machine not-via-DNS for Dns service => use instance id
        // TODO: Should we always use the instance id??

        InstanceBase instance = instances.findInstance(persistentInstance);
        if (instance == null) {
            // A machine has not (yet) been assigned
            throw new OpsException("Machine is not yet built").setRetry(TimeSpan.ONE_MINUTE);
        }
        return instance;
    }
}
