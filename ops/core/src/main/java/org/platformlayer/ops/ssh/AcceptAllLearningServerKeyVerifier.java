package org.platformlayer.ops.ssh;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.PublicKey;
import java.util.Map;

import com.google.common.collect.Maps;

public class AcceptAllLearningServerKeyVerifier extends AcceptAllServerKeyVerifier {
    private Map<SocketAddress, PublicKey> keys = Maps.newHashMap();
    private static final int SSH_PORT = 22;

    // public static final AcceptAllLearningServerKeyVerifier INSTANCE = new AcceptAllLearningServerKeyVerifier();

    public AcceptAllLearningServerKeyVerifier() {
    }

    @Override
    public boolean verifyServerKey(SocketAddress remoteAddress, PublicKey serverKey) {
        synchronized (keys) {
            keys.put(remoteAddress, serverKey);
        }

        return true;
    }

    public PublicKey getDiscoveredKey(SocketAddress remoteAddress) {
        synchronized (keys) {
            return keys.get(remoteAddress);
        }
    }

    public PublicKey getDiscoveredKey(InetAddress addr) {
        SocketAddress socketAddress = new InetSocketAddress(addr, SSH_PORT);
        return getDiscoveredKey(socketAddress);
    }

    @Override
    public void verifyPooled(IServerKeyVerifier serverKeyVerifier) {
        if (serverKeyVerifier instanceof AcceptAllLearningServerKeyVerifier) {
            synchronized (keys) {
                // Technically we should lock serverKeyVerifier...
                keys.putAll(((AcceptAllLearningServerKeyVerifier) serverKeyVerifier).keys);
            }
        }
    }

}
