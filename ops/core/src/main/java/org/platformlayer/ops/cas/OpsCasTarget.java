package org.platformlayer.ops.cas;

import org.platformlayer.cas.CasLocation;
import org.platformlayer.cas.CasTarget;
import org.platformlayer.ops.OpsTarget;

public class OpsCasTarget implements CasTarget {
	final OpsTarget target;

	public OpsCasTarget(OpsTarget target) {
		this.target = target;
	}

	@Override
	public CasLocation getLocation() {
		return new OpsCasLocation(target.getNetworkPoint());
	}

	public OpsTarget getOpsTarget() {
		return target;
	}

	public static OpsTarget getTarget(CasTarget destTarget) {
		return ((OpsCasTarget) destTarget).getOpsTarget();
	}

}
