package org.platformlayer.ops;

import org.slf4j.*;
import org.platformlayer.PlatformLayerClient;

public class PlatformLayerClientBuilder {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(PlatformLayerClientBuilder.class);

	final OpsSystem opsSystem;

	public PlatformLayerClientBuilder(OpsSystem opsSystem) {
		this.opsSystem = opsSystem;
	}

	private PlatformLayerClient platformLayerClient;

}
