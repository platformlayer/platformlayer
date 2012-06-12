package org.platformlayer.ops.filesystem;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import org.openstack.crypto.Md5Hash;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.cas.CasObject;
import org.platformlayer.ops.cas.CasStoreHelper;

public class DownloadFileByHash extends ManagedFile {
	public Md5Hash hash;

	@Inject
	CasStoreHelper casStore;

	@Override
	protected void uploadFile(OpsTarget target, File remoteFilePath) throws IOException, OpsException {
		target.mkdir(remoteFilePath.getParentFile());

		CasObject casObject;
		try {
			casObject = casStore.findArtifact(target, hash);
		} catch (Exception e) {
			throw new OpsException("Error while resolving artifact:" + hash, e);
		}
		if (casObject == null) {
			throw new OpsException("Unable to find artifact: " + hash);
		}

		casObject.copyTo(target, remoteFilePath);
	}

	@Override
	protected Md5Hash getSourceMd5(OpsTarget target) throws OpsException {
		if (hash == null) {
			throw new IllegalStateException("hash must be specified");
		}

		return hash;
	}
}
