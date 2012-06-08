package org.platformlayer.ops.cas;

import java.io.File;

import org.apache.log4j.Logger;
import org.platformlayer.crypto.Md5Hash;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.cas.filesystem.FilesystemCasObject;
import org.platformlayer.ops.cas.filesystem.FilesystemCasStore;
import org.platformlayer.ops.networks.NetworkPoint;

public abstract class CasObjectBase implements CasObject {
	static final Logger log = Logger.getLogger(CasObjectBase.class);

	private final Md5Hash hash;

	public CasObjectBase(Md5Hash hash) {
		this.hash = hash;
	}

	@Override
	public Md5Hash getHash() {
		return hash;
	}

	@Override
	public void copyTo(OpsTarget destTarget, File destPath) throws OpsException {
		NetworkPoint srcLocation = getLocation();
		NetworkPoint destLocation = destTarget.getNetworkPoint();

		int distance = NetworkPoint.estimateDistance(srcLocation, destLocation);
		log.info("Estimated distance from " + srcLocation + " to " + destLocation + " => " + distance);

		if (distance == 0) {
			this.copyTo(destTarget, destPath);
			return;
		}

		// TODO: Keep a CAS store on each host; don't put them into the guests
		FilesystemCasStore destCasStore = new FilesystemCasStore(destTarget);
		FilesystemCasObject onDest = destCasStore.copyToCache(this);

		onDest.copyTo(destTarget, destPath);
	}

	protected abstract void copyTo0(OpsTarget destTarget, File destPath) throws OpsException;
}
