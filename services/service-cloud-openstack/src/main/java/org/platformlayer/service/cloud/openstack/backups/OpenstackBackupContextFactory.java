package org.platformlayer.service.cloud.openstack.backups;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.backups.BackupContextFactory;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.machines.PlatformLayerCloudHelpers;
import org.platformlayer.ops.machines.StorageConfiguration;
import org.platformlayer.service.cloud.openstack.ops.OpenstackStorageConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenstackBackupContextFactory implements BackupContextFactory {

	private static final Logger log = LoggerFactory.getLogger(OpenstackBackupContextFactory.class);

	@Inject
	Provider<OpenstackBackupContext> backupContextProvider;

	@Inject
	InstanceHelpers instances;

	@Inject
	PlatformLayerCloudHelpers cloud;

	@Override
	public OpenstackBackupContext build(ItemBase item) throws OpsException {
		Machine machine = instances.findMachine(item);
		if (machine == null) {
			throw new OpsException("Cannot determine machine for: " + item);
		}
		StorageConfiguration storageConfiguration = cloud.getStorageConfiguration(machine);
		return build(storageConfiguration);
	}

	public OpenstackBackupContext build(StorageConfiguration storageConfiguration) throws OpsException {
		// TODO: Should be configurable
		// TODO: Configure cloud target here?
		String containerName = "backups";
		String backupId = UUID.randomUUID().toString();

		OpenstackBackupContext context = backupContextProvider.get();
		context.data.id = backupId;
		context.containerName = containerName;

		context.credentials = ((OpenstackStorageConfiguration) storageConfiguration).getOpenstackCredentials();

		return context;
	}
}
