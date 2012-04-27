package org.platformlayer.service.redis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.platformlayer.service.redis.model.RedisServer;
import org.platformlayer.tests.PlatformLayerApiTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ITRedisService extends PlatformLayerApiTest {
	@Override
	@BeforeMethod
	public void beforeMethod() {
		super.beforeMethod();

		getTypedItemMapper().addClass(RedisServer.class);
	}

	@Test
	public void testCreateAndDeleteItem() throws Exception {
		String id = "redis" + random.randomAlphanumericString(8);

		RedisServer redis = new RedisServer();
		redis.dnsName = id + ".test.platformlayer.org";

		redis = putItem(id, redis);
		redis = waitForHealthy(redis);

		InetSocketAddress socketAddress = getUniqueEndpoint(redis);
		Assert.assertFalse(isPortOpen(socketAddress));

		openFirewall(redis, socketAddress.getPort());

		String info = testRedis(socketAddress);
		Assert.assertTrue(info.contains("redis_version:"));
	}

	private String testRedis(InetSocketAddress socketAddress) throws IOException {
		Socket socket = new Socket();
		socket.connect(socketAddress);
		socket.getOutputStream().write("INFO\r\n".getBytes());

		StringBuilder sb = new StringBuilder();

		BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		while (true) {
			String line = reader.readLine();
			System.out.println("memcached said: " + line);
			if (line.equals("")) {
				break;
			}
			sb.append(line);
		}

		socket.close();

		return sb.toString();
	}

}
