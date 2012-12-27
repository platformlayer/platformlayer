package org.platformlayer.tests;

import java.io.IOException;

import org.platformlayer.TypedItemMapper;
import org.platformlayer.TypedPlatformLayerClient;
import org.platformlayer.ops.OpsException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.fathomdb.RandomUtil;

public abstract class AbstractPlatformLayerTest {
	private PlatformLayerTestContext context;
	protected RandomUtil random = new RandomUtil();

	protected abstract TypedItemMapper getTypedItemMapper();

	@BeforeMethod
	public void beforeMethod() {
		context = null;
	}

	@AfterMethod
	protected void afterMethod() throws IOException, OpsException {
		if (context != null) {
			context.cleanup();
		}
		context = null;
	}

	public TypedPlatformLayerClient getTypedClient() throws IOException, OpsException {
		return getContext().getTypedClient();
	}

	protected PlatformLayerTestContext getContext() {
		if (context == null) {
			context = PlatformLayerTestContext.buildFromProperties(getTypedItemMapper());
		}
		return context;
	}

}
