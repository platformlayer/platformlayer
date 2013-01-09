package org.platformlayer.ops.cas;

import java.io.File;

import org.slf4j.*;
import org.platformlayer.cas.CasLocation;
import org.platformlayer.cas.CasStore;
import org.platformlayer.cas.CasStoreObjectBase;
import org.platformlayer.cas.CasTarget;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.cas.filesystem.FilesystemCasObject;
import org.platformlayer.ops.cas.filesystem.FilesystemCasStore;

import com.fathomdb.hash.Md5Hash;

public abstract class OpsCasObjectBase extends CasStoreObjectBase {
	static final Logger log = LoggerFactory.getLogger(OpsCasObjectBase.class);

	public OpsCasObjectBase(CasStore store, Md5Hash hash) {
		super(store, hash);
	}

	@Override
	public void copyTo(CasTarget destTarget, File destPath, CasStore stagingStore) throws OpsException {
		CasLocation srcLocation = getLocation();
		CasLocation destLocation = destTarget.getLocation();

		int distance = srcLocation.estimateDistance(destLocation);
		log.info("Estimated distance from " + srcLocation + " to " + destLocation + " => " + distance);

		if (distance == 0) {
			log.info("Distance was zero; copying directly");
			this.copyTo0(OpsCasTarget.getTarget(destTarget), destPath);
			return;
		}

		if (stagingStore != null) {
			log.info("Staging object " + this + " to " + stagingStore);

			FilesystemCasObject staged = ((FilesystemCasStore) stagingStore).copyToStaging(this);

			staged.copyTo(destTarget, destPath, null);
		} else {
			log.info("Copying object from " + this + " to " + destTarget);

			this.copyTo0(OpsCasTarget.getTarget(destTarget), destPath);
		}
	}

	public abstract void copyTo0(OpsTarget destTarget, File destPath) throws OpsException;
}
