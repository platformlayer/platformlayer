package org.platformlayer.ops;

import java.security.PublicKey;
import java.util.List;

import org.platformlayer.core.model.HostPolicy;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tags;

public class MachineCreationRequest {
	public PlatformLayerKey cloud;

	public PublicKey sshPublicKey;
	public String sshPublicKeyName;

	public Tags tags;
	public PlatformLayerKey recipeId;
	public List<String> securityGroups;
	public int minimumMemoryMB;

	public HostPolicy hostPolicy;

	public String hostname;

	public List<Integer> publicPorts;
}
