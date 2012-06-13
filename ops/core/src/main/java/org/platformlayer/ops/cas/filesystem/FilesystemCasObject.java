package org.platformlayer.ops.cas.filesystem;

import java.io.File;

import org.openstack.crypto.ByteString;
import org.platformlayer.cas.CasLocation;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.cas.OpsCasLocation;
import org.platformlayer.ops.cas.OpsCasObjectBase;

public class FilesystemCasObject extends OpsCasObjectBase {
	private final FilesystemCasStore filesystemCasStore;
	private final File file;

	public FilesystemCasObject(ByteString hash, FilesystemCasStore filesystemCasStore, File file) {
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
	public CasLocation getLocation() {
		return new OpsCasLocation(filesystemCasStore.getLocation());
	}

}
