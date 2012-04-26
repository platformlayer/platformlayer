package org.platformlayer.tests;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import org.platformlayer.PlatformLayerClientBase;
import org.platformlayer.PlatformLayerUtils;
import org.platformlayer.TypedPlatformLayerClient;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Secret;
import org.platformlayer.ids.ManagedItemId;
import org.platformlayer.jobs.model.JobData;
import org.platformlayer.ops.OpsException;
import org.platformlayer.service.network.v1.NetworkConnection;
import org.platformlayer.xml.JaxbHelper;

public class PlatformLayerApiTest extends AbstractPlatformLayerTest {
	SimpleTypedItemMapper typedItemMapper;

	@Override
	protected void reset() {
		typedItemMapper = null;

		super.reset();
	}

	@Override
	protected SimpleTypedItemMapper getTypedItemMapper() {
		if (typedItemMapper == null) {
			typedItemMapper = new SimpleTypedItemMapper();
		}
		return typedItemMapper;
	}

	protected Secret randomSecret() {
		String s = random.randomAlphanumericString(8, 32);
		return Secret.build(s);
	}

	protected InetSocketAddress getEndpoint(ItemBase item) {
		List<String> endpoints = PlatformLayerUtils.findEndpoints(item.getTags());
		if (endpoints.size() != 1) {
			throw new IllegalStateException("Expected exactly one endpoint");
		}
		System.out.println("Found endpoint: " + endpoints.get(0));

		InetSocketAddress socketAddress = parseSocketAddress(endpoints.get(0));
		return socketAddress;
	}

	protected void openFirewall(ItemBase item, int port) throws OpsException, IOException {
		NetworkConnection firewallRule = new NetworkConnection();
		firewallRule.setSourceCidr("0.0.0.0/0");
		firewallRule.setDestItem(item.getKey());
		firewallRule.setPort(port);

		String id = item.getId() + "-global";
		firewallRule = putItem(id, firewallRule);

		waitForHealthy(firewallRule);
	}

	protected boolean isPortOpen(InetSocketAddress socketAddress) throws IOException {
		Socket socket = new Socket();
		try {
			int timeout = 5000;
			socket.connect(socketAddress, timeout);
			return true;
		} catch (IOException e) {
			String message = e.getMessage();
			if (message.equalsIgnoreCase("connect timed out")) {
				return false;
			}
			throw new IllegalStateException("Unexpected IO exception checking port status", e);
		} finally {
			socket.close();
		}
	}

	protected <T extends ItemBase> void deleteItem(T item) throws IOException, OpsException {
		TypedPlatformLayerClient client = getTypedClient();

		PlatformLayerKey key = item.getKey();
		client.deleteItem(key);
	}

	protected <T extends ItemBase> T putItem(String id, T item) throws OpsException, IOException {
		TypedPlatformLayerClient client = getTypedClient();

		Class<T> itemClass = (Class<T>) item.getClass();

		item.key = PlatformLayerKey.fromId(id);
		return client.putItem(item);
	}

	protected <T extends ItemBase> T getItem(String id, Class<T> itemClass) throws OpsException, IOException {
		TypedPlatformLayerClient client = getTypedClient();

		JaxbHelper jaxbHelper = PlatformLayerClientBase.toJaxbHelper(itemClass, new Class[] {});

		PlatformLayerKey key = PlatformLayerClientBase.toKey(jaxbHelper, new ManagedItemId(id), itemClass,
				client.listServices(true));
		return client.getItem(key, itemClass);
	}

	protected <T extends ItemBase> JobData doAction(T item, String actionName) throws OpsException, IOException {
		TypedPlatformLayerClient client = getTypedClient();

		PlatformLayerKey key = item.getKey();
		return client.doAction(key, actionName);
	}

	protected <T extends ItemBase> JobData doConfigure(T item) throws OpsException, IOException {
		return doAction(item, "configure");
	}

	protected InetSocketAddress parseSocketAddress(String s) {
		int lastColon = s.lastIndexOf(':');
		if (lastColon == -1) {
			throw new IllegalStateException();
		}

		String host = s.substring(0, lastColon);
		int port = Integer.parseInt(s.substring(lastColon + 1));

		InetAddress address;
		try {
			address = InetAddress.getByName(host);
		} catch (UnknownHostException e) {
			throw new IllegalStateException("Unable to resolve host: " + host, e);
		}

		return new InetSocketAddress(address, port);
	}

	protected <T extends ItemBase> T waitForHealthy(T item) throws OpsException, IOException {
		TypedPlatformLayerClient client = getTypedClient();

		Class<T> itemClass = (Class<T>) item.getClass();

		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new IllegalStateException("Interrupted", e);
			}

			T latest = client.getItem(item.getKey(), itemClass);
			switch (latest.getState()) {
			case ACTIVE:
				return latest;

			case BUILD:
			case CREATION_REQUESTED:
				System.out.println("Continuing to wait for " + item.getKey() + "; state=" + latest.getState());
				break;

			default:
				throw new IllegalStateException("Unexpected state: " + latest.getState() + " for " + latest);
			}
		}

	}

	protected JobData waitForJobComplete(JobData job) throws OpsException, IOException {
		TypedPlatformLayerClient client = getTypedClient();

		PlatformLayerKey jobKey = job.key;

		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new IllegalStateException("Interrupted", e);
			}

			// TODO: We really need a "get job status" function
			JobData found = null;
			for (JobData candidate : client.listJobs()) {
				if (jobKey.equals(candidate.key)) {
					found = candidate;
				}
			}

			if (found == null) {
				// Assume completed?
				throw new IllegalStateException("Job not found in job list");
			}

			switch (found.state) {
			case FAILED:
			case SUCCESS:
				return found;

			case RUNNING:
				System.out.println("Continuing to wait for " + job.key + "; state=" + found.state);
				break;

			default:
				throw new IllegalStateException("Unexpected state: " + found.state + " for " + job.key);
			}
		}

	}
}
