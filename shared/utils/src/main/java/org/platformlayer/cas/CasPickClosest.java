package org.platformlayer.cas;

import org.platformlayer.choice.ScoreChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CasPickClosest extends ScoreChooser<CasStoreObject, Integer> {
	private static final Logger log = LoggerFactory.getLogger(CasPickClosest.class);

	final CasLocation dest;

	public CasPickClosest(CasLocation dest) {
		super(FIND_MINIMUM);
		this.dest = dest;
	}

	@Override
	protected Integer score(CasStoreObject candidate) {
		try {
			return candidate.getLocation().estimateDistance(dest);
		} catch (Exception e) {
			log.warn("Error while trying to score CAS candidate: " + candidate, e);
			return Integer.MAX_VALUE;
		}
	}
}
