package org.platformlayer.ops.cas.filesystem;

import java.io.File;

import org.platformlayer.crypto.Md5Hash;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.cas.CasObjectBase;
import org.platformlayer.ops.networks.NetworkPoint;

public class FilesystemCasObject extends CasObjectBase {
	private final FilesystemCasStore filesystemCasStore;
	private final File file;

	public FilesystemCasObject(Md5Hash hash, FilesystemCasStore filesystemCasStore, File file) {
		super(hash);
		this.filesystemCasStore = filesystemCasStore;
		this.file = file;
	}

	@Override
	public void copyTo0(OpsTarget target, File remoteFilePath) throws OpsException {
		filesystemCasStore.copyTo(this, target, remoteFilePath);
	}

	public File getPath() {
		return file;
	}

	@Override
	public NetworkPoint getLocation() {
		return filesystemCasStore.getLocation();
	}

}
