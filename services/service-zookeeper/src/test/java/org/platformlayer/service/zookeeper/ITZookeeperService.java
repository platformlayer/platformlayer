package org.platformlayer.service.zookeeper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.platformlayer.IoUtils;
import org.platformlayer.service.zookeeper.model.ZookeeperCluster;
import org.platformlayer.tests.PlatformLayerApiTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ITZookeeperService extends PlatformLayerApiTest {

	@Override
	@BeforeMethod
	public void beforeMethod() {
		super.beforeMethod();

		getTypedItemMapper().addClass(ZookeeperCluster.class);
	}

	@Test
	public void testCreateAndDeleteItem() throws Exception {
		String id = "zk-" + random.randomAlphanumericString(8);

		ZookeeperCluster cluster = new ZookeeperCluster();
		cluster.dnsName = id + ".test.platformlayer.org";

		cluster = putItem(id, cluster);
		cluster = waitForHealthy(cluster);

		InetSocketAddress socketAddress = getFirstEndpoint(cluster);
		Assert.assertFalse(isPortOpen(socketAddress));

		openFirewall(cluster, socketAddress.getPort());
		Assert.assertTrue(isPortOpen(socketAddress));

		String response = testZookeeper(socketAddress);
		response = response.trim();
		Assert.assertTrue(response.contains("imok"));
	}

	private String testZookeeper(InetSocketAddress socketAddress) throws IOException {
		Socket socket = new Socket();
		socket.connect(socketAddress);
		socket.getOutputStream().write("ruok\n".getBytes());

		String response = IoUtils.readAll(socket.getInputStream());

		socket.close();

		return response;
	}
}
