package org.platformlayer.cas;

import org.apache.log4j.Logger;
import org.platformlayer.choice.ScoreChooser;
import org.platformlayer.ops.OpsException;

public class CasPickClosestStore extends ScoreChooser<CasStore, Integer> {
	private static final Logger log = Logger.getLogger(CasPickClosestStore.class);

	final CasLocation dest;

	public CasPickClosestStore(CasLocation dest) {
		super(FIND_MINIMUM);
		this.dest = dest;
	}

	@Override
	protected Integer score(CasStore candidate) {
		try {
			return candidate.estimateDistance(dest);
		} catch (OpsException e) {
			log.warn("Error while trying to score CAS candidate: " + candidate, e);
			return Integer.MAX_VALUE;
		}
	}
}
