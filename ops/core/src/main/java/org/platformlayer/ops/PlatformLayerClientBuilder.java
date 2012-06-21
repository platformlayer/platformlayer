package org.platformlayer.ops;

import org.apache.log4j.Logger;
import org.platformlayer.PlatformLayerClient;

public class PlatformLayerClientBuilder {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(PlatformLayerClientBuilder.class);

	final OpsSystem opsSystem;

	public PlatformLayerClientBuilder(OpsSystem opsSystem) {
		this.opsSystem = opsSystem;
	}

	private PlatformLayerClient platformLayerClient;

}
