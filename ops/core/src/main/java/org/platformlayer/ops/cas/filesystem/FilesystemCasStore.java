package org.platformlayer.ops.cas.filesystem;

import java.io.File;

import org.platformlayer.cas.CasLocation;
import org.platformlayer.cas.CasStore;
import org.platformlayer.cas.CasStoreObject;
import org.platformlayer.cas.CasStoreInfo;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.cas.OpsCasLocation;
import org.platformlayer.ops.cas.OpsCasObjectBase;
import org.platformlayer.ops.cas.OpsCasTarget;
import org.platformlayer.ops.filesystem.FilesystemInfo;
import org.platformlayer.ops.images.direct.PeerToPeerCopy;
import org.platformlayer.ops.networks.NetworkPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.hash.Md5Hash;

public class FilesystemCasStore implements CasStore {
	static final Logger log = LoggerFactory.getLogger(FilesystemCasStore.class);

	static final File PATH_BASE = new File("/var/cas");
	static final File PATH_SEEDS = new File(PATH_BASE, "seeds");
	static final File PATH_CACHE = new File(PATH_BASE, "cache");

	final OpsTarget host;

	private final CasStoreInfo info;

	public FilesystemCasStore(CasStoreInfo info, OpsCasTarget destTarget) {
		this.info = info;
		this.host = destTarget.getOpsTarget();
	}

	String toRelativePath(Md5Hash hash, int splits) {
		String hex = hash.toHex();

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < splits; i++) {
			sb.append(hex.subSequence(i * 2, (i + 1) * 2));
			sb.append("/");
		}
		sb.append(hex);

		return sb.toString();
	}

	@Override
	public FilesystemCasObject findArtifact(Md5Hash hash) throws OpsException {
		File file = checkDirectory(PATH_SEEDS, hash, 0);
		if (file != null) {
			return new FilesystemCasObject(this, hash, file);
		}

		file = checkDirectory(PATH_CACHE, hash, 2);
		if (file != null) {
			return new FilesystemCasObject(this, hash, file);
		}

		return null;
	}

	private File checkDirectory(File base, Md5Hash hash, int splits) throws OpsException {
		String relativePath = toRelativePath(hash, splits);
		File seedFile = new File(base, relativePath);
		FilesystemInfo seedFileInfo = host.getFilesystemInfoFile(seedFile);
		if (seedFileInfo != null) {
			Md5Hash seedFileHash = host.getFileHash(seedFile);
			if (!seedFileHash.equals(hash)) {
				log.warn("Hash mismatch on file: " + seedFile);
				return null;
			}

			// For LRU
			host.touchFile(seedFile);

			return seedFile;
		}
		return null;
	}

	public FilesystemCasObject copyToStaging(CasStoreObject src) throws OpsException {
		Md5Hash hash = src.getHash();
		File cachePath = new File(PATH_CACHE, toRelativePath(hash, 2));
		host.mkdir(cachePath.getParentFile());

		// This could be copyTo0, but it serves as a nice test that copyTo is falling through correctly
		// src.copyTo(host, cachePath);
		// TODO: We're confused by multiple IP addresses (e.g. IPV4 vs IPV6)
		// TODO: Fix, revert to copyTo, make copyTo0 protected
		((OpsCasObjectBase) src).copyTo0(host, cachePath);

		return new FilesystemCasObject(this, hash, cachePath);
	}

	public CasLocation getLocation() {
		return new OpsCasLocation(getNetworkPoint());
	}

	public NetworkPoint getNetworkPoint() {
		return this.host.getNetworkPoint();
	}

	void copyTo(FilesystemCasObject src, OpsTarget target, File targetFilePath, boolean cacheOnTarget)
			throws OpsException {
		File fileOnTarget;

		if (!host.isSameMachine(target)) {
			File downloadTo;

			if (cacheOnTarget) {
				// Copy to host cache
				File cachePath = new File(PATH_CACHE, toRelativePath(src.getHash(), 2));

				target.mkdir(cachePath.getParentFile());
				downloadTo = cachePath;
			} else {
				downloadTo = targetFilePath;
			}

			PeerToPeerCopy peerToPeerCopy = Injection.getInstance(PeerToPeerCopy.class);
			peerToPeerCopy.copy(host, src.getPath(), target, downloadTo);

			fileOnTarget = downloadTo;
		} else {
			fileOnTarget = src.getPath();
		}

		if (!fileOnTarget.equals(targetFilePath)) {
			Command copy = Command.build("cp {0} {1}", fileOnTarget, targetFilePath);
			target.executeCommand(copy);
		} else {
			log.info("File is in destination path: " + fileOnTarget);
		}
	}

	@Override
	public Md5Hash findTag(String tag) {
		return null;
	}

	@Override
	public int estimateDistance(CasLocation target) throws OpsException {
		return getLocation().estimateDistance(target);
	}

	@Override
	public String toString() {
		return "FilesystemCasStore [host=" + host + "]";
	}

	@Override
	public CasStoreInfo getOptions() {
		return info;
	}
}
