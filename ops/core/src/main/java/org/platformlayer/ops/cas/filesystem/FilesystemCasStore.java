package org.platformlayer.ops.cas.filesystem;

import java.io.File;

import org.apache.log4j.Logger;
import org.platformlayer.crypto.Md5Hash;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.cas.CasStore;
import org.platformlayer.ops.filesystem.FilesystemInfo;
import org.platformlayer.ops.images.direct.PeerToPeerCopy;

public class FilesystemCasStore implements CasStore {
	static final Logger log = Logger.getLogger(FilesystemCasStore.class);

	static final File PATH_BASE = new File("/var/cas");
	static final File PATH_SEEDS = new File(PATH_BASE, "seeds");
	static final File PATH_CACHE = new File(PATH_BASE, "cache");

	final OpsTarget host;

	public FilesystemCasStore(OpsTarget host) {
		this.host = host;
	}

	String toRelativePath(Md5Hash hash) {
		String hex = hash.toHex();
		return hex.substring(0, 2) + "/" + hex.substring(2, 4) + "/" + hex;
	}

	@Override
	public FilesystemCasObject findArtifact(Md5Hash hash) throws Exception {
		File file = checkDirectory(PATH_SEEDS, hash);
		if (file != null) {
			return new FilesystemCasObject(this, file, hash);
		}

		file = checkDirectory(PATH_CACHE, hash);
		if (file != null) {
			return new FilesystemCasObject(this, file, hash);
		}

		return null;
	}

	private File checkDirectory(File base, Md5Hash hash) throws OpsException {
		String relativePath = toRelativePath(hash);
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

	public void copyTo(FilesystemCasObject src, OpsTarget target, File targetFilePath) throws OpsException {
		File targetSrc;

		if (!host.equals(target)) {
			// Copy to host cache
			File cachePath = new File(PATH_CACHE, toRelativePath(src.getHash()));

			target.mkdir(cachePath.getParentFile());

			PeerToPeerCopy peerToPeerCopy = Injection.getInstance(PeerToPeerCopy.class);
			peerToPeerCopy.copy(host, src.getPath(), target, cachePath);

			targetSrc = cachePath;
		} else {
			targetSrc = src.getPath();
		}

		Command copy = Command.build("cp {0} {1}", targetSrc, targetFilePath);
		target.executeCommand(copy);
	}
}
