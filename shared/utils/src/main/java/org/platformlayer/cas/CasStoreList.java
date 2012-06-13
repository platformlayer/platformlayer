package org.platformlayer.cas;

import java.util.List;

import org.apache.log4j.Logger;
import org.openstack.crypto.ByteString;

import com.google.common.collect.Lists;

public class CasStoreList {
	private static final Logger log = Logger.getLogger(CasStoreList.class);

	final List<CasStore> primaryList = Lists.newArrayList();
	final List<CasStore> secondaryList = Lists.newArrayList();

	public CasStoreObject findArtifact(CasTarget target, ByteString hash) throws Exception {
		for (CasStore casStore : primaryList) {
			CasStoreObject found = tryFind(casStore, hash);
			if (found != null) {
				// Any primary match is good enough to stop looking...
				return found;
			}
		}

		List<CasStoreObject> matches = Lists.newArrayList();

		for (CasStore casStore : secondaryList) {
			CasStoreObject found = tryFind(casStore, hash);
			if (found != null) {
				matches.add(found);
			}
		}

		CasPickClosest chooser = new CasPickClosest(target.getLocation());

		if (log.isDebugEnabled()) {
			log.debug("Found " + matches.size() + " CAS copies");
			for (CasStoreObject match : matches) {
				log.debug("\t" + match + " => " + chooser.score(match));
			}
		}

		return chooser.choose(matches);
	}

	private CasStoreObject tryFind(CasStore casStore, ByteString hash) {
		try {
			CasStoreObject uri = casStore.findArtifact(hash);
			if (uri != null) {
				return uri;
			}
		} catch (Exception e) {
			log.warn("Error while resolving artifact in " + casStore, e);
		}
		return null;
	}

	public void addPrimary(CasStore primary) {
		primaryList.add(primary);
	}

	public void add(CasStore secondary) {
		secondaryList.add(secondary);
	}
}
