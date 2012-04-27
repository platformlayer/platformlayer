package org.platformlayer.service.openldap;

import org.openstack.client.utils.RandomUtil;
import org.platformlayer.core.model.Secret;
import org.platformlayer.tests.PlatformLayerTestContext;

public class TestHelper {
	final PlatformLayerTestContext context;
	final RandomUtil random;

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
