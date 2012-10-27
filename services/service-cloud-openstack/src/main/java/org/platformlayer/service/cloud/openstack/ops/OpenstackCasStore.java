package org.platformlayer.service.cloud.openstack.ops;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.openstack.client.OpenstackCredentials;
import org.openstack.client.OpenstackNotFoundException;
import org.openstack.client.common.OpenstackSession;
import org.openstack.client.storage.OpenstackStorageClient;
import org.openstack.crypto.ByteString;
import org.openstack.crypto.Md5Hash;
import org.openstack.model.storage.StorageObject;
import org.platformlayer.cas.CasLocation;
import org.platformlayer.cas.CasStore;
import org.platformlayer.cas.CasStoreObject;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.cas.OpsCasObjectBase;

import com.google.common.collect.Lists;

public class OpenstackCasStore implements CasStore {
	static final Logger log = Logger.getLogger(OpenstackCasStore.class);

	final String containerName;
	final OpenstackCredentials credentials;

	public OpenstackCasStore(OpenstackCredentials credentials, String containerName) {
		this.credentials = credentials;
		this.containerName = containerName;
	}

	@Override
	public CasStoreObject findArtifact(ByteString hash) {
		OpenstackStorageClient storageClient = getStorageClient();

		try {
			List<StorageObject> storageObjects = Lists.newArrayList(storageClient
					.listObjects(containerName, null, null));

			String findHash = hash.toHex();
			for (StorageObject storageObject : storageObjects) {
				String storageObjectHash = storageObject.getHash();
				if (storageObjectHash.equalsIgnoreCase(findHash)) {
					return new OpenstackCasObject(storageObject);
				}
			}
		} catch (OpenstackNotFoundException e) {
			log.debug("Not found (404) returned from Openstack");
			return null;
		}

		return null;
	}

	private OpenstackStorageClient getStorageClient() {
		OpenstackStorageClient storageClient = getSession().getStorageClient();
		return storageClient;
	}

	OpenstackSession session;

	private OpenstackSession getSession() {
		if (this.session == null) {
			OpenstackSession session = OpenstackSession.create();
			session.authenticate(getCredentials(), true);
			this.session = session;
		}
		return this.session;
	}

	public class OpenstackCasObject extends OpsCasObjectBase {
		final StorageObject storageObject;

		public OpenstackCasObject(StorageObject storageObject) {
			super(OpenstackCasStore.this, new Md5Hash(storageObject.getHash()));
			this.storageObject = storageObject;
		}

		@Override
		public void copyTo0(OpsTarget target, File remoteFilePath) throws OpsException {
			String objectPath = storageObject.getName();

			DirectOpenstackDownload download = new DirectOpenstackDownload();
			download.download(target, remoteFilePath, getCredentials(), containerName, objectPath);
		}

		@Override
		public CasLocation getLocation() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString() {
			return "OpenstackCasObject [storageObject=" + storageObject + "]";
		}
	}

	public OpenstackCredentials getCredentials() {
		return credentials;
	}

	@Override
	public ByteString findTag(String tag) {
		return null;
	}

	@Override
	public int estimateDistance(CasLocation target) throws OpsException {
		// TODO: Fix
		log.warn("Hard-coding distance from Openstack CAS as 4");
		return 4;
	}

}
