package org.platformlayer.service.cloud.openstack.ops;

import org.openstack.client.OpenstackCredentials;
import org.platformlayer.ops.machines.StorageConfiguration;

public class OpenstackStorageConfiguration implements StorageConfiguration {

	private final OpenstackCredentials credentials;

	public OpenstackStorageConfiguration(OpenstackCredentials credentials) {
		this.credentials = credentials;
	}

	public OpenstackCredentials getOpenstackCredentials() {
		return credentials;
	}
}
