package org.platformlayer.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Properties;

import org.openstack.client.utils.RandomUtil;
import org.openstack.utils.Io;
import org.platformlayer.DirectPlatformLayerClient;
import org.platformlayer.EndpointInfo;
import org.platformlayer.IoUtils;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientBase;
import org.platformlayer.TypedItemMapper;
import org.platformlayer.TypedPlatformLayerClient;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ids.ManagedItemId;
import org.platformlayer.jobs.model.JobData;
import org.platformlayer.ops.OpsException;
import org.platformlayer.service.network.v1.NetworkConnection;
import org.platformlayer.xml.JaxbHelper;

import com.google.common.collect.Lists;

public class PlatformLayerTestContext {
	PlatformLayerClient platformLayerClient;

	TypedPlatformLayerClient typedClient;

	final File configFile;

	final TypedItemMapper typedItemMapper;

	final List<ItemBase> ownedItems = Lists.newArrayList();

	public final RandomUtil random = new RandomUtil();

	public PlatformLayerTestContext(File configFile, TypedItemMapper typedItemMapper) {
		this.configFile = configFile;
		this.typedItemMapper = typedItemMapper;
	}

	public static PlatformLayerTestContext buildFromProperties(TypedItemMapper typedItemMapper) {
		String config = System.getProperty("config");
		if (config == null) {
			config = "tests";
		}

		if (!config.contains(File.separator)) {
			config = "~/.credentials/" + config;
		}

		File configFile = Io.resolve(config);
		return new PlatformLayerTestContext(configFile, typedItemMapper);
	}

	public PlatformLayerClient buildPlatformLayerClient() throws IOException, OpsException {
		PlatformLayerClient client;
		if (configFile == null) {
			throw new IllegalArgumentException("Config file is required");
		}

		InputStream is = null;
		try {
			if (!configFile.exists()) {
				throw new FileNotFoundException("Configuration file not found: " + configFile);
			}

			is = new FileInputStream(configFile);

			Properties properties = new Properties();
			try {
				properties.load(is);
			} catch (IOException e) {
				throw new IOException("Error reading configuration file", e);
			}
			client = DirectPlatformLayerClient.buildUsingProperties(properties);
		} finally {
			if (is != System.in) {
				IoUtils.safeClose(is);
			}
		}

		return client;
	}

	public PlatformLayerClient getUntypedClient() throws IOException, OpsException {
		if (platformLayerClient == null) {
			platformLayerClient = buildPlatformLayerClient();
		}
		return platformLayerClient;
	}

	public TypedPlatformLayerClient getTypedClient() throws IOException, OpsException {
		if (typedClient == null) {
			typedClient = new TypedPlatformLayerClient(getUntypedClient(), getMapper());
		}
		return typedClient;
	}

	private TypedItemMapper getMapper() {
		return typedItemMapper;
	}

	public <T extends ItemBase> T putItem(String id, T item) throws OpsException, IOException {
		TypedPlatformLayerClient client = getTypedClient();

		Class<T> itemClass = (Class<T>) item.getClass();

		item.key = PlatformLayerKey.fromId(id);
		T put = client.putItem(item);
		ownedItems.add(put);
		return put;
	}

	public <T extends ItemBase> T getItem(String id, Class<T> itemClass) throws OpsException, IOException {
		TypedPlatformLayerClient client = getTypedClient();

		JaxbHelper jaxbHelper = PlatformLayerClientBase.toJaxbHelper(itemClass, new Class[] {});

		PlatformLayerKey key = PlatformLayerClientBase.toKey(jaxbHelper, new ManagedItemId(id), itemClass,
				client.listServices(true));
		return client.getItem(key, itemClass);
	}

	public void cleanup() throws IOException, OpsException {
		while (!ownedItems.isEmpty()) {
			ItemBase item = ownedItems.remove(ownedItems.size() - 1);
			deleteItem(item);
		}
	}

	public <T extends ItemBase> void deleteItem(T item) throws IOException, OpsException {
		TypedPlatformLayerClient client = getTypedClient();

		PlatformLayerKey key = item.getKey();
		client.deleteItem(key);
	}

	public <T extends ItemBase> JobData doAction(T item, String actionName) throws OpsException, IOException {
		TypedPlatformLayerClient client = getTypedClient();

		PlatformLayerKey key = item.getKey();
		return client.doAction(key, actionName);
	}

	public <T extends ItemBase> JobData doConfigure(T item) throws OpsException, IOException {
		return doAction(item, "configure");
	}

	public <T extends ItemBase> T waitForHealthy(T item) throws OpsException, IOException {
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

	public NetworkConnection openFirewall(ItemBase item, int port) throws OpsException, IOException {
		NetworkConnection firewallRule = new NetworkConnection();
		firewallRule.setSourceCidr("0.0.0.0/0");
		firewallRule.setDestItem(item.getKey());
		firewallRule.setPort(port);

		String id = item.getId() + "-global";
		firewallRule = putItem(id, firewallRule);

		waitForHealthy(firewallRule);

		return firewallRule;
	}

	public boolean isPortOpen(InetSocketAddress socketAddress) throws IOException {
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

	public InetSocketAddress parseSocketAddress(String s) {
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

	public InetSocketAddress toSocketAddress(EndpointInfo endpoint) {
		String host = endpoint.publicIp;

		InetAddress address;
		try {
			address = InetAddress.getByName(host);
		} catch (UnknownHostException e) {
			throw new IllegalStateException("Unable to resolve host: " + host, e);
		}

		return new InetSocketAddress(address, endpoint.port);
	}

	public JobData waitForJobComplete(JobData job) throws OpsException, IOException {
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

	private InetSocketAddress getEndpoint(ItemBase item, boolean unique) {
		List<EndpointInfo> endpoints = EndpointInfo.getEndpoints(item.getTags());
		if (unique) {
			if (endpoints.size() != 1) {
				throw new IllegalStateException("Expected exactly one endpoint");
			}
			System.out.println("Found endpoint: " + endpoints.get(0));
		} else {
			if (endpoints.size() == 0) {
				throw new IllegalStateException("Expected at least one endpoint");
			}
			System.out.println("Found endpoints: " + endpoints + "; choosing: " + endpoints.get(0));
		}

		InetSocketAddress socketAddress = toSocketAddress(endpoints.get(0));
		return socketAddress;
	}

	public InetSocketAddress getUniqueEndpoint(ItemBase item) {
		return getEndpoint(item, true);
	}

	public InetSocketAddress getFirstEndpoint(ItemBase item) {
		return getEndpoint(item, false);
	}

}
