package org.platformlayer.ops.cas;

import java.io.File;

import org.apache.log4j.Logger;
import org.openstack.crypto.ByteString;
import org.platformlayer.cas.CasLocation;
import org.platformlayer.cas.CasStoreObjectBase;
import org.platformlayer.cas.CasTarget;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.cas.filesystem.FilesystemCasObject;
import org.platformlayer.ops.cas.filesystem.FilesystemCasStore;

public abstract class OpsCasObjectBase extends CasStoreObjectBase {
	static final Logger log = Logger.getLogger(OpsCasObjectBase.class);

	public OpsCasObjectBase(ByteString hash) {
		super(hash);
	}

	@Override
	public void copyTo(CasTarget destTarget, File destPath) throws Exception {
		CasLocation srcLocation = getLocation();
		CasLocation destLocation = destTarget.getLocation();

		int distance = srcLocation.estimateDistance(destLocation);
		log.info("Estimated distance from " + srcLocation + " to " + destLocation + " => " + distance);

		if (distance == 0) {
			this.copyTo0(OpsCasTarget.getTarget(destTarget), destPath);
			return;
		}

		// TODO: Keep a CAS store on each host; don't put them into the guests
		FilesystemCasStore destCasStore = new FilesystemCasStore((OpsCasTarget) destTarget);
		FilesystemCasObject onDest = destCasStore.copyToCache(this);

		onDest.copyTo(destTarget, destPath);
	}

	public abstract void copyTo0(OpsTarget destTarget, File destPath) throws OpsException;
}
