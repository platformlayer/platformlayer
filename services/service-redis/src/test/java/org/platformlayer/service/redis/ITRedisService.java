package org.platformlayer.service.redis;

import java.net.InetSocketAddress;
import java.util.List;

import org.platformlayer.PlatformLayerUtils;
import org.platformlayer.service.network.v1.NetworkConnection;
import org.platformlayer.service.redis.model.RedisServer;
import org.platformlayer.service.redis.ops.RedisServerController;
import org.platformlayer.tests.PlatformLayerApiTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ITRedisService extends PlatformLayerApiTest {

    @BeforeMethod
    public void beforeMethod() {
        reset();

        getTypedItemMapper().addClass(RedisServer.class);
    }

    @Test
    public void testCreateAndDeleteItem() throws Exception {
        String id = random.randomAlphanumericString(8);

        RedisServer create = new RedisServer();
        create.dnsName = id + ".test.platformlayer.org";

        RedisServer created = putItem(id, create);

        RedisServer healthy = waitForHealthy(created);

        List<String> endpoints = PlatformLayerUtils.findEndpoints(healthy.getTags());

        if (endpoints.size() != 1) {
            throw new IllegalStateException("Expected exactly one endpoint");
        }

        InetSocketAddress socketAddress = parseSocketAddress(endpoints.get(0));

        Assert.assertFalse(isPortOpen(socketAddress));

        NetworkConnection firewallRule = new NetworkConnection();
        firewallRule.setSourceCidr("0.0.0.0/0");
        firewallRule.setDestItem(created.getKey());
        firewallRule.setPort(RedisServerController.PORT);

        firewallRule = putItem(id, firewallRule);

        waitForHealthy(firewallRule);

        // Socket socket = new Socket();
        // socket.connect(socketAddress);
        // socket.getOutputStream().write("stats\n".getBytes());
        //
        // BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        // while (true) {
        // String line = reader.readLine();
        // System.out.println("memcached said: " + line);
        // if (line.equals("END")) {
        // break;
        // }
        // if (line.equals("ERROR")) {
        // throw new IllegalStateException("Got ERROR reply from memcache");
        // }
        // }
        //
        // socket.close();

        deleteItem(created);
    }

}
