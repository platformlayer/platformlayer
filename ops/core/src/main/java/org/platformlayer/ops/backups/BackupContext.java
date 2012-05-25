package org.platformlayer.ops.backups;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.openstack.client.OpenstackCredentials;
import org.openstack.client.OpenstackException;
import org.openstack.client.common.OpenstackSession;
import org.openstack.client.storage.OpenstackStorageClient;
import org.openstack.utils.Utf8;
import org.platformlayer.ops.OpsException;
import org.platformlayer.xml.JaxbHelper;

public class BackupContext {
	BackupData data = new BackupData();
	String containerName;

	OpenstackCredentials credentials;

	public void add(BackupItem item) {
		this.data.items.add(item);
	}

	public String getBackupId() {
		return this.data.id;
	}

	public String toPath(String objectName) {
		return getBackupId() + "/" + objectName;
	}

	public String getContainerName() {
		return containerName;
	}

	private void ensureContainer() {
		getStorageClient().root().containers().create(containerName);
	}

	private OpenstackSession openstackSession = null;

	public OpenstackSession getOpenstackSession() {
		if (openstackSession == null) {
			OpenstackSession session = OpenstackSession.create();

			session.authenticate(credentials, false);

			openstackSession = session;

			ensureContainer();
		}
		return openstackSession;
	}

	OpenstackStorageClient storageClient;

	OpenstackStorageClient getStorageClient() {
		if (storageClient == null) {
			storageClient = getOpenstackSession().getStorageClient();
		}
		return storageClient;
	}

	public void writeDescriptor() throws OpsException {
		String path = getBackupId() + ".backup.xml";

		JaxbHelper jaxb = JaxbHelper.get(BackupData.class);
		String xml;
		try {
			xml = jaxb.marshal(data, true);
		} catch (JAXBException e) {
			throw new OpsException("Error serializing metadata", e);
		}
		byte[] data = Utf8.getBytes(xml);

		try {
			getStorageClient().putObject(containerName, path, data);
		} catch (OpenstackException e) {
			throw new OpsException("Error uploading metadata", e);
		} catch (IOException e) {
			throw new OpsException("Error uploading metadata", e);
		}
	}

}
