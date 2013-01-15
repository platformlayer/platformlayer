package org.platformlayer.ops.dns;

import java.net.InetAddress;
import java.util.List;

import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.networks.NetworkPoint;

public interface DnsResolverProvider {

	List<InetAddress> findAddresses(NetworkPoint from) throws OpsException;

}
