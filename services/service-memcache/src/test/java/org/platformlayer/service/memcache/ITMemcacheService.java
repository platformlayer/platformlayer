package org.platformlayer.service.memcache;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import org.platformlayer.PlatformLayerUtils;
import org.platformlayer.TypedPlatformLayerClient;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.OpsException;
import org.platformlayer.service.memcache.model.MemcacheServer;
import org.platformlayer.service.memcache.ops.MemcacheServerController;
import org.platformlayer.service.network.v1.NetworkConnection;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ITMemcacheService extends PlatformLayerApiTest {

    @BeforeMethod
    public void beforeMethod() {
        reset();

        getTypedItemMapper().addClass(MemcacheServer.class);
    }

    @Test
    public void testCreateAndDeleteItem() throws Exception {
        String id = random.randomAlphanumericString(8);

        MemcacheServer create = new MemcacheServer();
        create.dnsName = id + ".test.platformlayer.org";

        MemcacheServer created = putItem(id, create);

        MemcacheServer healthy = waitForHealthy(created);

        List<String> endpoints = PlatformLayerUtils.findEndpoints(healthy.getTags());

        if (endpoints.size() != 1) {
            throw new IllegalStateException("Expected exactly one endpoint");
        }

        InetSocketAddress socketAddress = parseSocketAddress(endpoints.get(0));

        assertPortNotOpen(socketAddress);

        NetworkConnection firewallRule = new NetworkConnection();
        firewallRule.setSourceCidr("0.0.0.0/0");
        firewallRule.setDestItem(created.getKey());
        firewallRule.setPort(MemcacheServerController.MEMCACHE_PORT);

        firewallRule = putItem(id, firewallRule);

        waitForHealthy(firewallRule);

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

        deleteItem(created);
    }

    private void assertPortNotOpen(InetSocketAddress socketAddress) throws IOException {
        Socket socket = new Socket();
        try {
            int timeout = 5000;
            socket.connect(socketAddress, timeout);
        } catch (IOException e) {
            String message = e.getMessage();
            if (message.equals("connect timed out")) {
                return;
            }
        } finally {
            socket.close();
        }

        Assert.fail("Socket was open: " + socketAddress);
    }

    private <T extends ItemBase> void deleteItem(T item) throws IOException, OpsException {
        TypedPlatformLayerClient client = getTypedClient();

        PlatformLayerKey key = item.getKey();
        client.deleteItem(key);
    }

    private <T extends ItemBase> T putItem(String id, T item) throws OpsException, IOException {
        TypedPlatformLayerClient client = getTypedClient();

        Class<T> itemClass = (Class<T>) item.getClass();

        item.key = PlatformLayerKey.fromId(id);
        return client.putItem(item);
    }

    private InetSocketAddress parseSocketAddress(String s) {
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

    private <T extends ItemBase> T waitForHealthy(T item) throws OpsException, IOException {
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
                System.out.println("Continuing to wait; state=" + latest.getState());
                break;

            default:
                throw new IllegalStateException("Unexpected state: " + latest.getState() + " for " + latest);
            }
        }

    }

}
