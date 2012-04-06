package org.platformlayer.ops.backups;

import java.io.IOException;
import java.util.UUID;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import org.openstack.client.OpenstackCredentials;
import org.openstack.client.OpenstackException;
import org.openstack.client.common.OpenstackSession;
import org.openstack.client.storage.OpenstackStorageClient;
import org.openstack.utils.Utf8;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.machines.PlatformLayerCloudHelpers;
import org.platformlayer.ops.machines.StorageConfiguration;
import org.platformlayer.xml.JaxbHelper;

public class BackupContext {
    BackupData data = new BackupData();
    String containerName;

    OpenstackCredentials credentials;

    public static BackupContext build(ItemBase item) throws OpsException {
        // TODO: Should be configurable
        // TODO: Configure cloud target here?
        String containerName = "backups";
        String backupId = UUID.randomUUID().toString();

        BackupContext context = Injection.getInstance(BackupContext.class);
        context.data.id = backupId;
        context.containerName = containerName;

        Machine machine = context.instances.findMachine(item);

        StorageConfiguration storageConfiguration = context.cloud.getStorageConfiguration(machine);
        context.credentials = storageConfiguration.getOpenstackCredentials();
        return context;
    }

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

    // private RemoteCurlOpenstackSession getOpenstackSession(OpsTarget target) {
    // if (openstackSession == null) {
    // RemoteCurlOpenstackSession session = new RemoteCurlOpenstackSession(target);
    //
    // session.authenticate(credentials, false);
    //
    // openstackSession = session;
    // }
    // return openstackSession;
    // }

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

    @Inject
    InstanceHelpers instances;

    @Inject
    PlatformLayerCloudHelpers cloud;

}
