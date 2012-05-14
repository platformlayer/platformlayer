package org.platformlayer.ops.cas.filesystem;

import java.io.File;

import org.platformlayer.crypto.Md5Hash;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.cas.CasObject;

public class FilesystemCasObject implements CasObject {
	private final FilesystemCasStore filesystemCasStore;
	private final File file;
	private final Md5Hash hash;

	public FilesystemCasObject(FilesystemCasStore filesystemCasStore, File file, Md5Hash hash) {
		this.filesystemCasStore = filesystemCasStore;
		this.file = file;
		this.hash = hash;
	}

	@Override
	public void copyTo(OpsTarget target, File remoteFilePath) throws OpsException {
		filesystemCasStore.copyTo(this, target, remoteFilePath);
	}

	public File getPath() {
		return file;
	}

	public Md5Hash getHash() {
		return hash;
	}

}
