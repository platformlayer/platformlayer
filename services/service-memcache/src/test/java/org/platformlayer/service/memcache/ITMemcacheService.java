package org.platformlayer.service.memcache;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.platformlayer.service.memcache.model.MemcacheServer;
import org.platformlayer.service.memcache.ops.MemcacheServerController;
import org.platformlayer.tests.PlatformLayerApiTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ITMemcacheService extends PlatformLayerApiTest {

	@Override
	@BeforeMethod
	public void beforeMethod() {
		super.beforeMethod();

		getTypedItemMapper().addClass(MemcacheServer.class);
	}

	@Test
	public void testCreateAndDeleteItem() throws Exception {
		String id = random.randomAlphanumericString(8);

		MemcacheServer memcache = new MemcacheServer();
		memcache.dnsName = id + ".test.platformlayer.org";

		memcache = putItem(id, memcache);
		memcache = waitForHealthy(memcache);

		InetSocketAddress socketAddress = getEndpoint(memcache);
		Assert.assertFalse(isPortOpen(socketAddress));

		openFirewall(memcache, MemcacheServerController.MEMCACHE_PORT);

		testMemcache(socketAddress);
	}

	private void testMemcache(InetSocketAddress socketAddress) throws IOException {
		Socket socket = new Socket();
		socket.connect(socketAddress);
		socket.getOutputStream().write("stats\n".getBytes());

		BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		while (true) {
			String line = reader.readLine();
			System.out.println("memcached said: " + line);
			if (line.equals("END")) {
				break;
			}
			if (line.equals("ERROR")) {
				throw new IllegalStateException("Got ERROR reply from memcache");
			}
		}

		socket.close();
	}

}
