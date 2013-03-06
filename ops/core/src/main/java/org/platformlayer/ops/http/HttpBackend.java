package org.platformlayer.ops.http;

import java.net.URI;

import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.networks.NetworkPoint;

public interface HttpBackend {
	URI getUri(NetworkPoint src) throws OpsException;
}
