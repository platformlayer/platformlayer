package org.platformlayer.ops.endpoints;

import com.google.common.base.Objects;

public class EndpointInfo {
	public String publicIp;
	public Integer port;

	public boolean matches(Integer port) {
		if (this.port == null || port == null || port == 0) {
			return true;
		}
		if (!Objects.equal(port, this.port)) {
			return false;
		}
		return true;
	}
}
