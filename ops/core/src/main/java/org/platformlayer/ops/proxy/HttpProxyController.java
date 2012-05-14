package org.platformlayer.ops.proxy;

import java.net.URI;

import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.networks.NetworkPoint;

public interface HttpProxyController {
	/**
	 * Gets the proxy to use for specified url. Returns null if the specified URI cannot be proxied / cached by the
	 * specified proxy (e.g. apt-cacher-ng can't do port 8080, by default)
	 */
	String getUrl(Object model, NetworkPoint forNetworkPoint, URI uri) throws OpsException;
}
