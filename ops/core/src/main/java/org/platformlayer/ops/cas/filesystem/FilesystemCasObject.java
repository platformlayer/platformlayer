package org.platformlayer.ops.cas.filesystem;

import java.io.File;

import org.openstack.crypto.ByteString;
import org.platformlayer.cas.CasLocation;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.cas.OpsCasObjectBase;

public class FilesystemCasObject extends OpsCasObjectBase {
	private final File file;

	public FilesystemCasObject(FilesystemCasStore store, ByteString hash, File file) {
		super(store, hash);
		this.file = file;
	}

	@Override
	public void copyTo0(OpsTarget target, File remoteFilePath) throws OpsException {
		copyFilesystem(target, remoteFilePath, false);
	}

	public void copyFilesystem(OpsTarget target, File remoteFilePath, boolean cacheOnTarget) throws OpsException {
		getStore().copyTo(this, target, remoteFilePath, cacheOnTarget);
	}

	public File getPath() {
		return file;
	}

	@Override
	public CasLocation getLocation() {
		return getStore().getLocation();
	}

	@Override
	public String toString() {
		return "FilesystemCasObject [store=" + getStore() + ", file=" + file + "]";
	}

	@Override
	public FilesystemCasStore getStore() {
		return (FilesystemCasStore) super.getStore();
	}

}
