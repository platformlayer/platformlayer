package org.platformlayer.ops.networks;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;

public class NetworkPoint {
    final String privateNetworkId;
    final InetAddress address;

    // For now, we assume there's one private network
    public static final String PRIVATE_NETWORK_ID = "private";

    public NetworkPoint(String privateNetworkId, InetAddress address) {
        this.privateNetworkId = privateNetworkId;
        this.address = address;
    }

    public static String getMyNetworkKey() {
        // We assume we're on the private network
        return PRIVATE_NETWORK_ID;
    }

    public static NetworkPoint forSameNetwork(InetAddress address) {
        return new NetworkPoint(getMyNetworkKey(), address);
    }

    public static NetworkPoint forMe() {
        // TODO: This is probably not the address we really want...
        InetAddress myAddress;
        try {
            myAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Cannot get my IP", e);
        }
        return forSameNetwork(myAddress);
    }

    public static NetworkPoint forTarget(OpsTarget target) {
        return target.getNetworkPoint();
    }

    public static NetworkPoint forPublicInternet() {
        return new NetworkPoint(null, null);
    }

    public static NetworkPoint forPublicHostname(String hostname) throws OpsException {
        InetAddress address;
        try {
            address = InetAddress.getByName(hostname);
        } catch (UnknownHostException e) {
            throw new OpsException("Error resolving hostname", e);
        }
        return new NetworkPoint(null, address);
    }

    public static NetworkPoint forTargetInContext() {
        OpsTarget target = OpsContext.get().getInstance(OpsTarget.class);

        return forTarget(target);
    }

    public String getPrivateNetworkId() {
        return privateNetworkId;
    }

    public InetAddress getAddress() {
        return address;
    }

    public boolean isPublicInternet() {
        return privateNetworkId == null;
    }

    public static NetworkPoint forNetwork(String network) {
        return new NetworkPoint(network, null);
    }

    @Override
    public String toString() {
        return "NetworkPoint [privateNetworkId=" + privateNetworkId + ", address=" + address.getHostAddress() + "]";
    }

}
