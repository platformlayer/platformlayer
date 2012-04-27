package org.platformlayer.tests;

import org.openstack.client.utils.RandomUtil;
import org.platformlayer.core.model.Secret;

public class TestHelper {
	protected final PlatformLayerTestContext context;
	protected final RandomUtil random;

	public TestHelper(PlatformLayerTestContext context) {
		super();
		this.context = context;
		this.random = context.random;
	}

	public Secret randomSecret() {
		String s = random.randomAlphanumericString(8, 32);
		return Secret.build(s);
	}

}
