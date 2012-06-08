package org.platformlayer.ops.cas;

import org.apache.log4j.Logger;
import org.platformlayer.choice.ScoreChooser;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.networks.NetworkPoint;

public class CasPickClosest extends ScoreChooser<CasObject, Integer> {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(CasPickClosest.class);

	final OpsTarget target;

	final NetworkPoint targetNetworkPoint;

	public CasPickClosest(OpsTarget target) {
		super(FIND_MINIMUM);
		this.target = target;

		this.targetNetworkPoint = target.getNetworkPoint();
	}

	@Override
	protected Integer score(CasObject candidate) {
		try {
			NetworkPoint objectLocation = candidate.getLocation();

			return NetworkPoint.estimateDistance(objectLocation, targetNetworkPoint);
		} catch (OpsException e) {
			log.warn("Error while trying to score CAS candidate: " + candidate, e);
			return Integer.MAX_VALUE;
		}
	}
}
