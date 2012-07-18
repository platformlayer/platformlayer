package org.platformlayer.ops.filesystem;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import org.openstack.crypto.ByteString;
import org.openstack.crypto.Md5Hash;
import org.platformlayer.cas.CasStoreList;
import org.platformlayer.cas.CasStoreObject;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.cas.CasStoreHelper;
import org.platformlayer.ops.cas.OpsCasTarget;

public class DownloadFileByHash extends ManagedFile {
	public Md5Hash hash;

	public String specifier;

	@Inject
	CasStoreHelper casStore;

	Md5Hash resolved;

	public String getHumanName() {
		if (hash == null) {
			return specifier;
		}
		return hash.toHex();
	}

	public Md5Hash getResolved(OpsTarget target) throws OpsException {
		if (resolved == null) {
			if (hash == null) {
				CasStoreList casStores = casStore.getCasStores(target);
				resolved = (Md5Hash) casStores.resolve(specifier);
				if (resolved == null) {
					throw new OpsException("Unable to resolve artifact: " + getHumanName());
				}
				return resolved;
			} else {
				resolved = hash;
			}
		}
		return resolved;
	}

	@Override
	protected void uploadFile(OpsTarget target, File remoteFilePath) throws IOException, OpsException {
		target.mkdir(remoteFilePath.getParentFile());

		ByteString resolved = getResolved(target);

		CasStoreObject casObject;
		try {
			CasStoreList casStores = casStore.getCasStores(target);
			casObject = casStores.findArtifact(new OpsCasTarget(target), resolved);
		} catch (Exception e) {
			throw new OpsException("Error while resolving artifact:" + getHumanName(), e);
		}

		if (casObject == null) {
			throw new OpsException("Unable to find artifact: " + getHumanName());
		}

		try {
			casObject.copyTo(new OpsCasTarget(target), remoteFilePath);
		} catch (Exception e) {
			throw new OpsException("Error copying file to remote CAS", e);
		}
	}

	@Override
	protected Md5Hash getSourceMd5(OpsTarget target) throws OpsException {
		Md5Hash resolved = getResolved(target);

		if (resolved == null) {
			throw new IllegalStateException("Hash could not be determined (file could not be resolved?)");
		}

		return resolved;
	}
}
