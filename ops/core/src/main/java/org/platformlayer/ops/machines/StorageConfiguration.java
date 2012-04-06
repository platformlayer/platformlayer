package org.platformlayer.ops.machines;

import org.openstack.client.OpenstackCredentials;

public class StorageConfiguration {

    private final OpenstackCredentials credentials;

    public StorageConfiguration(OpenstackCredentials credentials) {
        this.credentials = credentials;
    }

    public OpenstackCredentials getOpenstackCredentials() {
        return credentials;
    }
}
