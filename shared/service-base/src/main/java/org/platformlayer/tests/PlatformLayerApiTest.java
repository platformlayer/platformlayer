package org.platformlayer.tests;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.platformlayer.TimeSpan;
import org.platformlayer.common.Job;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.jobs.model.JobData;
import org.platformlayer.ops.OpsException;
import org.platformlayer.service.network.v1.NetworkConnection;
import org.testng.annotations.BeforeMethod;

public class PlatformLayerApiTest extends AbstractPlatformLayerTest {
	SimpleTypedItemMapper typedItemMapper;

	@Override
	@BeforeMethod
	public void beforeMethod() {
		typedItemMapper = null;

		super.beforeMethod();
	}

	@Override
	protected SimpleTypedItemMapper getTypedItemMapper() {
		if (typedItemMapper == null) {
			typedItemMapper = new SimpleTypedItemMapper();
		}
		return typedItemMapper;
	}

	protected <T extends ItemBase> T getItem(String id, Class<T> itemClass) throws OpsException, IOException {
		return getContext().getItem(id, itemClass);
	}

	protected <T extends ItemBase> void deleteItem(T item) throws IOException, OpsException {
		getContext().deleteItem(item);
	}

	protected <T extends ItemBase> T putItem(String id, T item) throws OpsException, IOException {
		return getContext().putItem(id, item);
	}

	protected <T extends ItemBase> T waitForHealthy(T item) throws OpsException, IOException {
		return getContext().waitForHealthy(item);
	}

	protected boolean isPortOpen(InetSocketAddress socketAddress) throws IOException {
		return getContext().isPortOpen(socketAddress);
	}

	protected NetworkConnection openFirewall(ItemBase item, int port) throws OpsException, IOException {
		return getContext().openFirewall(item, port);
	}

	protected InetSocketAddress getUniqueEndpoint(ItemBase item) {
		return getContext().getUniqueEndpoint(item);
	}

	protected InetSocketAddress getFirstEndpoint(ItemBase item) {
		return getContext().getFirstEndpoint(item);
	}

	protected Job waitForJobComplete(JobData job, TimeSpan timeout) throws OpsException, IOException {
		return getContext().waitForJobComplete(job, timeout);
	}
}
