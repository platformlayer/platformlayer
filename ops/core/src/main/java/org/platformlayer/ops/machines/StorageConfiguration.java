package org.platformlayer.ops.machines;

import java.util.Map;

public interface StorageConfiguration {

	Map<String, String> getProperties();

	// private final OpenstackCredentials credentials;
	//
	// public StorageConfiguration(OpenstackCredentials credentials) {
	// this.credentials = credentials;
	// }
	//
	// public OpenstackCredentials getOpenstackCredentials() {
	// return credentials;
	// }
}
