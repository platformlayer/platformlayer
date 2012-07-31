package org.platformlayer.ops.cas;

import org.platformlayer.cas.CasLocation;
import org.platformlayer.ops.networks.NetworkPoint;

public class OpsCasLocation implements CasLocation {

	final NetworkPoint networkPoint;

	public OpsCasLocation(NetworkPoint networkPoint) {
		this.networkPoint = networkPoint;
	}

	@Override
	public int estimateDistance(CasLocation destLocation) {
		NetworkPoint other = ((OpsCasLocation) destLocation).networkPoint;
		return NetworkPoint.estimateDistance(this.networkPoint, other);
	}

	@Override
	public String toString() {
		return "OpsCasLocation [networkPoint=" + networkPoint + "]";
	}
}
