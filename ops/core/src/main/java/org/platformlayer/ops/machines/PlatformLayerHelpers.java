package org.platformlayer.ops.machines;

import javax.inject.Inject;

import org.platformlayer.PlatformLayerClient;
import org.platformlayer.TypedPlatformLayerClient;
import org.platformlayer.ops.Injection;

public class PlatformLayerHelpers extends TypedPlatformLayerClient {
	@Inject
	ServiceProviderHelpers serviceProviderHelpers;

	@Inject
	public PlatformLayerHelpers(PlatformLayerClient platformLayerClient, ServiceProviderHelpers serviceProviderHelpers) {
		super(platformLayerClient, new PlatformLayerTypedItemMapper(serviceProviderHelpers));
		this.serviceProviderHelpers = serviceProviderHelpers;
	}

	public static PlatformLayerHelpers build(PlatformLayerClient client) {
		ServiceProviderHelpers serviceProviderHelpers = Injection.getInstance(ServiceProviderHelpers.class);
		return new PlatformLayerHelpers(client, serviceProviderHelpers);
	}

}
