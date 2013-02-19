package org.platformlayer.service.cloud.openstack.ops;

import java.util.Map;

import org.openstack.client.OpenstackCredentials;
import org.platformlayer.ops.machines.StorageConfiguration;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public class OpenstackStorageConfiguration implements StorageConfiguration {

	private final OpenstackCredentials credentials;

	public OpenstackStorageConfiguration(OpenstackCredentials credentials) {
		this.credentials = credentials;
	}

	public OpenstackCredentials getOpenstackCredentials() {
		return credentials;
	}

	@Override
	public Map<String, String> getProperties() {
		Map<String, String> properties = Maps.newHashMap();

		properties.put("url", credentials.getAuthUrl());
		properties.put("user", credentials.getUsername());
		properties.put("key", credentials.getSecret());
		if (!Strings.isNullOrEmpty(credentials.getTenant())) {
			properties.put("tenant", credentials.getTenant());
		}

		return properties;
	}
}
