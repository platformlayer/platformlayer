package org.platformlayer.ops.filesystem;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import org.openstack.crypto.Md5Hash;
import org.platformlayer.cas.CasStoreList;
import org.platformlayer.cas.CasStoreObject;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.cas.CasStoreHelper;
import org.platformlayer.ops.cas.OpsCasTarget;

public class DownloadFileByHash extends ManagedFile {
	public Md5Hash hash;

	@Inject
	CasStoreHelper casStore;

	@Override
	protected void uploadFile(OpsTarget target, File remoteFilePath) throws IOException, OpsException {
		target.mkdir(remoteFilePath.getParentFile());

		CasStoreObject casObject;
		try {
			CasStoreList casStores = casStore.getCasStores(target);
			casObject = casStores.findArtifact(new OpsCasTarget(target), hash);
		} catch (Exception e) {
			throw new OpsException("Error while resolving artifact:" + hash, e);
		}
		if (casObject == null) {
			throw new OpsException("Unable to find artifact: " + hash);
		}

		try {
			casObject.copyTo(new OpsCasTarget(target), remoteFilePath);
		} catch (Exception e) {
			throw new OpsException("Error copying file to remote CAS", e);
		}
	}

	@Override
	protected Md5Hash getSourceMd5(OpsTarget target) throws OpsException {
		if (hash == null) {
			throw new IllegalStateException("hash must be specified");
		}

		return hash;
	}
}
