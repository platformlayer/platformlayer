package org.platformlayer.ops.machines;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.InstanceBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.Tags;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.MachineBase;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.endpoints.EndpointHelpers;
import org.platformlayer.ops.endpoints.EndpointInfo;
import org.platformlayer.ops.networks.NetworkPoint;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

public class PlatformLayerCloudMachine extends MachineBase {
    static final Logger log = Logger.getLogger(PlatformLayerCloudMachine.class);

    final PlatformLayerHelpers platformLayerClient;
    final InstanceBase machine;

    public PlatformLayerCloudMachine(PlatformLayerHelpers platformLayerClient, InstanceBase machine) {
        this.platformLayerClient = platformLayerClient;
        this.machine = machine;
    }

    @Override
    public void terminate() throws OpsException {
        platformLayerClient.deleteItem(OpsSystem.toKey(machine));

        // context.terminate(machine);
    }

    @Override
    public PlatformLayerKey getKey() {
        return OpsSystem.toKey(machine);
    }

    public InstanceBase getInstance() {
        return this.machine;
    }

    // public String getPublicAddress(ItemBase item) throws OpsException {
    // String address = findPublicAddress(item);
    // if (address == null) {
    // throw new OpsException("Could not determine public address for: " + item);
    // }
    // return address;
    // }

    @Override
    public String findAddress(NetworkPoint src, int destinationPort) {
        String privateNetworkId = src.getPrivateNetworkId();
        if (Objects.equal(privateNetworkId, NetworkPoint.PRIVATE_NETWORK_ID)) {
            Tags tags = machine.getTags();
            for (String address : tags.find(Tag.NETWORK_ADDRESS)) {
                if (address.contains(":")) {
                    log.info("Ignoring IPV6 address: " + address);
                    continue;
                }
                if (!Strings.isNullOrEmpty(address))
                    return address;
            }
        }

        // if (src.isPublicInternet())
        // We assume that private networks can still reach the public internet, so these work for everyone
        {
            EndpointHelpers endpointHelpers = Injection.getInstance(EndpointHelpers.class);
            EndpointInfo endpoint = endpointHelpers.findEndpoint(machine.getTags(), destinationPort);
            if (endpoint != null)
                return endpoint.publicIp;
        }

        return null;

        // OpsContext ops = OpsContext.get();
        // ModelKey modelKey = ops.buildModelKey(item);
        //
        //
        //
        // // {
        // // String instanceKey = tags.findUnique(Tag.INSTANCE_KEY);
        // //
        // // if (instanceKey != null) {
        // // Machine machine = cloud.findMachineByInstanceKey(instanceKey);
        // // return machine;
        // // }
        // // }
        //
        // {
        // // TODO: Do we have to skip this if we've been passed a PersistentInstances?
        //
        // // String conductorId = ops.buildUrl(modelKey);
        //
        // Tag parentTag = ops.createParentTag(modelKey);
        //
        // // // TODO: Fix this so that we don't get everything...
        // // for (PersistentInstance persistentInstance : platformLayer.listItems(PersistentInstance.class)) {
        // // String systemId = persistentInstance.getTags().findUnique(Tag.PARENT_ID);
        // // if (Objects.equal(conductorId, systemId)) {
        // // String instanceKey = persistentInstance.getTags().findUnique(Tag.INSTANCE_KEY);
        // // if (instanceKey != null) {
        // // return cloud.findMachineByInstanceKey(instanceKey);
        // // }
        // // }
        // // }
        //
        // // for (PersistentInstance persistentInstance : platformLayer.listItems(PersistentInstance.class, parentTag))
        // {
        // // String instanceKey = persistentInstance.getTags().findUnique(Tag.INSTANCE_KEY);
        // // if (instanceKey != null) {
        // // return cloud.findMachineByInstanceKey(instanceKey);
        // // }
        // // }
        //
        // }
        // }

        // Tags tags = machine.getTags();
        // for (Tag tag : tags) {
        // if (tag.getKey().equals(Tag.NETWORK_ADDRESS)) {
        // return tag.getValue();
        // }
        // }
        // return null;
        // }
        //
        // return null;
        // }

        // if ()
        // String privateNetworkId = src.getPrivateNetworkId();
        // if (privateNetworkId)
        // Tags tags = machine.getTags();
        // for (Tag tag : tags) {
        // if (tag.getKey().equals(Tag.NETWORK_ADDRESS)) {
        // return tag.getValue();
        // }
        // }
        // return null;
    }
}
